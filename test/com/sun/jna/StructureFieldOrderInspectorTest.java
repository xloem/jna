package com.sun.jna;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Test utility class for inspecting {@link com.sun.jna.Structure#getFieldOrder()} methods.
 *
 * @author Dan Rollo
 * Date: 1/17/13
 * Time: 4:29 PM
 */
public class StructureFieldOrderInspectorTest extends TestCase {

    private String origPropJNANoSys;

    protected void setUp() {
        origPropJNANoSys = System.getProperty("jna.nosys");
        System.setProperty("jna.nosys", "true"); // would be set by ant script, set here for IDE usage
    }

    protected void tearDown() {
        if (origPropJNANoSys == null) {
            System.getProperties().remove("jna.nosys");
        } else {
            System.setProperty("jna.nosys", origPropJNANoSys);
        }
    }


    public void testFindStructureSubClasses() {

        final Set<Class<? extends Structure>> classes = StructureFieldOrderInspector.findSubTypesOfStructure(Platform.class);

        assertTrue("Found no Structure sub types.", classes.size() > 0);

        for (final Class<? extends Structure> structureSubType : classes) {
            assertTrue(structureSubType.getName(), Structure.class.isAssignableFrom(structureSubType));
        }
    }

    public void testCheckMethodGetFieldOrderExisting() {
        StructureFieldOrderInspector.checkMethodGetFieldOrder(StructureByValueTest.TestNativeMappedInStructure.class, null);
    }

    public void testCheckMethodGetFieldOrderTagInterface() {
        StructureFieldOrderInspector.checkMethodGetFieldOrder(StructureByValueTest.TestNativeMappedInStructure.ByValue.class, null);
    }


    private static final class MyStructMissingField extends Structure {
        @SuppressWarnings("UnusedDeclaration")
        public String missingDeclaredField;

        @Override
        protected List getFieldOrder() {
            //noinspection unchecked
            return Arrays.asList();
        }
    }
    public void testCheckMethodGetFieldOrderMissingField() throws Exception {
        try {
            StructureFieldOrderInspector.checkMethodGetFieldOrder(MyStructMissingField.class, null);
            fail("Expected Error: Structure.getFieldOrder()...");
        } catch (RuntimeException e) {
            assertTrue(e.getCause().getCause().getMessage().contains("not match declared field names"));
        }
    }

    private static final class MyStructExtraField extends Structure {
        @Override
        protected List getFieldOrder() {
            return Arrays.asList("extraField");
        }
    }
    public void testCheckMethodGetFieldOrderExtraField() throws Exception {
        try {
            StructureFieldOrderInspector.checkMethodGetFieldOrder(MyStructExtraField.class, null);
            fail("Expected Error: Structure.getFieldOrder()...");
        } catch (RuntimeException e) {
            assertTrue(e.getCause().getCause().getMessage().contains("not match declared field names"));
        }
    }


    public void testCheckMethodGetFieldOrderWithAbstractSubtype() throws Exception {
        StructureFieldOrderInspector.checkMethodGetFieldOrder(Union.class, null);
    }

    public void testCheckStructureGetFieldOrder() throws Exception {
        StructureFieldOrderInspector.checkStructureGetFieldOrder(Platform.class, null);
    }

    public void testBatchCheckStructureGetFieldOrder() throws Exception {
        try {
            StructureFieldOrderInspector.batchCheckStructureGetFieldOrder(StructureTest.class, null);
            fail("Expected structure failures");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().startsWith("Some Structure sub types"));
        }
    }
}
