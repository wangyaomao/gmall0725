package com.atguigu.gmall.cart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallCartWebApplicationTests {

	@Test
	public void contextLoads() {

		// 价格计算

		// 初始化
		BigDecimal b1 = new BigDecimal(0.01d);
		BigDecimal b2 = new BigDecimal(0.01f);

		System.out.println(b1);
		System.out.println(b2);

		BigDecimal b3 = new BigDecimal("0.01");

		System.out.println(b3);

		// 大小比较
		int i = b2.compareTo(b1);// -1 0 1
		System.out.println(i);

		// 计算
		BigDecimal b4 = new BigDecimal("6");
		BigDecimal b5 = new BigDecimal("7");

		BigDecimal add = b4.add(b5);

		System.out.println(add);

		BigDecimal subtract = b4.subtract(b5);

		System.out.println(subtract);

		BigDecimal multiply = b4.multiply(b5);

		System.out.println(multiply);



		// 约等于
		BigDecimal divide = b4.divide(b5,2,BigDecimal.ROUND_HALF_DOWN);

		System.out.println(divide);

		BigDecimal add1 = b1.add(b2);

		System.out.println(add1);

		BigDecimal bigDecimal = add1.setScale(2, BigDecimal.ROUND_HALF_DOWN);

		System.out.println(bigDecimal);

	}

}
