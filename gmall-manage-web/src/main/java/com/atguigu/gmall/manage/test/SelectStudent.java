package com.atguigu.gmall.manage.test;

import java.util.Random;

public class SelectStudent {

    public static void main(String[] args) {
        Random row = new Random();
        int rowNum = row.nextInt(7)+2;

        Random column = new Random();
        int columnNum = column.nextInt(9)+1;

        System.out.println("第"+rowNum+"行，第"+columnNum+"列");
    }
}
