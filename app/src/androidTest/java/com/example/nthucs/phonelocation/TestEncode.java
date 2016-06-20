package com.example.nthucs.phonelocation;

/**
 * Created by NTHUCS on 2016/6/20.
 */
public class TestEncode {
    public static void main(String[] args) {
        double A = 2.475907, B = 6.111120, C = 9.999999;
        int[][] rect = Encode.encode(A, C);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(rect[i][j]);
            }
            System.out.println();
        }
    }
}
