package edu.unh.cs.cs619.bulletzone.datalayer;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.Map;

import edu.unh.cs.cs619.bulletzone.datalayer.permission.Permission;

public class PermissionTest {

    @Test
    public void getPermissionMapping_usingIntegerGet_returnsConsistentMap() {
        Map<Integer, String> m = Permission.getPermissionMapping();
        for (Permission p : Permission.values())
            assertThat(m.get(p.ordinal()), is(p.name()));
    }

    @Test
    public void get_usingStringGet_returnsConsistentValues() {
        for (Permission p : Permission.values())
            assertThat(Permission.get(p.name()), is (p));
    }
}