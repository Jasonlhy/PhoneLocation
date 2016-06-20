
package com.example.nthucs.phonelocation;

public class Decoder {

    private double longitude;
    private double latitude;

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void decode(int[][] dataArray){
            System.out.println("\n hello user");
            int row, column, parity, errorCounterRow, errorCounterColumn, sw;
            int errorRow[] = new int[9];
            int errorColumn[] = new int[9];
            double power;

            double latitude, longitude;

            for (row = 0; row <= 7; row++) {
                for (column = 0; column <= 7; column++) {
                    dataArray[row][column] = 1;//input 1 or 0 into the array 1 by 1
                }
            }

            //	dataArray[3][3]++;//for test the error correction

            errorCounterRow = 0;//initialization
            for (row = 0; row <= 7; row++) {
                parity = 0;//initialization,0 is default and 1 is for test
                for (column = 0; column < 7; column++) {
                    parity = parity + dataArray[row][column];
                    //System.out.println("\n parity is "+parity);// delete this row
                    parity = parity % 2;// parity is a boolean
                    //System.out.println("\n parity mod 2 is "+parity);// delete this row
                }
                //System.out.println(" row is "+row+"Data [row][7] is "+dataArray[row][7]+" parity is "+parity);// delete this row
                if (dataArray[row][7] != parity) {
                    System.out.println("\n Error occurs at row " + row);
                    errorCounterRow++;
                    errorRow[errorCounterRow] = row;

                }
                if (errorCounterRow >= 2)
                    System.out.println("\n Errors occur at rows " + errorCounterRow + " places");
            }//row parity check


            errorCounterColumn = 0;//initialization again
            for (column = 0; column <= 7; column++) {
                parity = 0;//initialization,0 is default and 1 is for test.
                for (row = 0; row < 7; row++) {
                    parity = parity + dataArray[row][column];
                    //System.out.println("\n parity is "+parity);// delete this row
                    parity = parity % 2;// parity is a boolean
                    //System.out.println("\n parity mod 2 is "+parity);// delete this row
                }
                //System.out.println(" row is "+row+"Data [row][7] is "+dataArray[row][7]+" parity is "+parity);// delete this row
                if (dataArray[7][column] != parity) {
                    System.out.println("\n Error occurs at column " + column);
                    errorCounterColumn++;
                    errorColumn[errorCounterColumn] = column;

                }
                if (errorCounterColumn >= 2)
                    System.out.println("\n Errors occur at columns " + errorCounterColumn + " places");
            }//column parity check


            if ((errorCounterColumn > 1) || (errorCounterRow > 1)) {
                System.out.println("more than 1 errors occur");//Adding the feedback and stop the program at here.
                for (row = 0; row <= 7; row++) {
                    for (column = 0; column < 7; column++) {
                        dataArray[row][column] = 0;//When multiple error occurs, feedback a all-0 array.
                    }
                }
                //Adding a call function here
            } else if ((errorCounterColumn == 0) && (errorCounterRow == 0))
                System.out.println("no error occurs");
            else {
                dataArray[errorRow[1]][errorColumn[1]]++;
                dataArray[errorRow[1]][errorColumn[1]] = dataArray[errorRow[1]][errorColumn[1]] % 2;
                System.out.println("\nthe error at row " + errorRow[1] + " column " + errorColumn[1] + " change as " + dataArray[errorRow[1]][errorColumn[1]]);
            }// error correction and detection

            //decoding

            latitude = 0;//initialization
            power = 0;//initialization
            sw = 23;//switcher,be used for switching the calculation from latitude to longitude. Each 24 bits, for 2 to the power 0~23, respectively.
            longitude = 0;//initialization
            for (row = 6; row >= 0; row--) {
                for (column = 6; column >= 0; column--) {
                    if (row == 6 && column > 5)
                        column = 5;//data end at the 48th bit,every 24bits demonstrate one dimension(2 to the power 0~23)
                    if (sw >= 0) {
                        sw--;//switcher
                        latitude = latitude + Math.pow(2, power);
                        System.out.println(" latitude " + latitude + " power " + power);
                        power++;
                    } else {
                        longitude = longitude + Math.pow(2, power - 24);//Each dimension uses 23 bits.
                        System.out.println(" longitude " + longitude + " power " + (power - 24));
                        power++;
                    }
                }
            }//decoding, converting binary to double(w/o position shift)

            double finalLongitude = (longitude / 1000000 + 20);
            double finalLatitude = (latitude / 1000000 + 120);

            this.latitude = finalLatitude;
            this.longitude = finalLongitude;

            System.out.println(" longitude " + this.latitude + " latitude " + this.longitude + " N ");// shift to the real longitude and latitude

    }
}// }of class
