package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DemoTest {
    @Test
    public void testAdd() {
        Demo demo = new Demo();
        assertEquals(5, demo.add(2, 3)); // 测试 2+3=5
    }
}
