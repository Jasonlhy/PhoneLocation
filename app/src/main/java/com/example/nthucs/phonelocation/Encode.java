package com.example.nthucs.phonelocation;

public class Encode {
    public static void main(String[] args) {
        double A = 2.475907, B = 6.111120, C = 9.999999;
        int[][] rect = encode(A, C);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(rect[i][j]);
            }
            System.out.println();
        }
    }

    public static int[][] encode(double latitude, double longtitude) {
        int[][] rect = new int[8][8];

        int[][] temp = new int[2][24];
        //int[][] rect = new int[8][8];
        toBinary(latitude, temp[0]);
        toBinary(longtitude, temp[1]);
        /*
		//for(int num:temp[0])System.out.print(num);
		for(int i=23;i>=0 ;i--)System.out.print(temp[0][i]);
		System.out.println();
		for(int i=23;i>=0 ;i--)System.out.print(temp[1][i]);
		*/

        int count = 47;//48:rect[6][6]=0
        for (int i = 0; i < temp[1].length; i++) {
            rect[count / 7][count % 7] = temp[1][i];
            count--;
        }

        for (int i = 0; i < temp[0].length; i++) {
            rect[count / 7][count % 7] = temp[0][i];
            count--;
        }

        int checkBit = 0;
        for (int i = 0; i < 7; i++) {
            checkBit = 0;
            for (int j = 0; j < 7; j++) {
                checkBit += rect[i][j];
            }
            rect[i][7] = checkBit % 2;
        }

        for (int i = 0; i < 8; i++)//col
        {
            checkBit = 0;
            for (int j = 0; j < 7; j++) {
                checkBit += rect[j][i];
            }
            rect[7][i] = checkBit % 2;
        }

        return rect;
    }

    public static void toBinary(double A, int[] B) {
        int a = (int) (A * 1000000);
        int count = 0;
        while (a != 0) {
            B[count] = a % 2;
            a >>= 1;
            count++;
        }
    }
}