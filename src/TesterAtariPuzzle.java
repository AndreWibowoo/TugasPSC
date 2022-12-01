
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Dionisius Salvavictori Wanggur / 2017730005
 * ref : https://www.researchgate.net/publication/220174496_A_Nested_Two-steps_Evolutionary_Algorithm_for_the_Light-up_Puzzle
 *      https://towardsdatascience.com/how-to-define-a-fitness-function-in-a-genetic-algorithm-be572b9ea3b4
 *      https://towardsdatascience.com/introduction-to-genetic-algorithms-including-example-code-e396e98d8bf3
 */
public class TesterAtariPuzzle {

    static int m; //ukuran puzzle (jumlah baris)

    static int n; //ukuran puzzle (jumlah kolom)

    static int fitness = 0; 

    static String[][] puzzle = null; //papan puzzle light-up
    static int[][] target;
    ArrayList<Integer> kotakAngka;
    ArrayList<Integer> probability;
    ArrayList<Integer> indexI;
    ArrayList<Integer> IndexJ;
    HashMap<Integer, Integer> index;

    PopulasiPuzzle population = new PopulasiPuzzle();
    IndividuPuzzle fittest;
    IndividuPuzzle secondFittest;
    int generationCount = 0;

    public static void main(String[] args) throws FileNotFoundException {
        //menyimpan file inputan pengguna pada sebuah variable file untuk dibaca oleh system 
        File file = new File("D:\\Kuliah\\Pengantar Sistem Cerdas\\Tugas PSC Contoh\\input.txt");
        BufferedReader in;

        //jika filenya ada program akan menjalankan method solve, jika tidak akan menampilkan warning
        if (file.exists()) {
            if (file != null) {
                in = new BufferedReader(new FileReader(file));
            } else {
                in = new BufferedReader(new InputStreamReader(System.in));
            }
            //panggil method parsa untuk membaca file .txt
            parse(in);
            //array target yang digunakan untuk membuat individu
            target = new int[m][n];
            
            //mengatur kromosom yang akan dibuat dalam bentuk 2D
            for (int i = 0; i < puzzle.length; i++) {
                for (int j = 0; j < puzzle[0].length; j++) {
                    /**
                     * jika pada papan puzzle bertanda 'X', menunjukan kotak hitam yg tidak memiliki angka
                     * jika pada papan puzzle bertanda '0', untuk setiap kotak yang bertetangga akan ditandai dengan angka 5
                     * yang menunjukan kotak tersebut akan diterangi oleh lampu (tidak akan berisi lampu)
                     * jika pada papan puzzle bukan/tidak bertanda '-', menunjukan kotak tersebut memiliki value tertentu di dalamnya
                     * selain hal di atas, pada array 'target' setiap kotak akan diisi dengan angka 0.
                     */
                    if (puzzle[i][j].equalsIgnoreCase("X")) {
                        target[i][j] = 2;
                    } else if (puzzle[i][j].equalsIgnoreCase("0")) {
                        if (i - 1 >= 0) {
                            target[i - 1][j] = 5;
                        }
                        if (i + 1 < puzzle.length) {
                            target[i + 1][j] = 5;
                        }
                        if (j - 1 >= 0) {

                            target[i][j - 1] = 5;
                        }
                        if (j + 1 < puzzle.length) {
                            target[i][j + 1] = 5;
                        }
                        target[i][j] = 2;
                    } else if (!puzzle[i][j].equalsIgnoreCase("-")) {
                        target[i][j] = 2;
                    } else if (target[i][j] != 2 && target[i][j] != 5) {
                        target[i][j] = 0;
                    }
                    //puzzle di print sebagai gambaran awal dari puzzle
                    System.out.print(puzzle[i][j] + " ");
                }
                System.out.println();
            }
        } else {
            System.out.println("Warning! : Harus Masukin Inputan Gan!");
        }
        
        //inisiasi Random
        Random rn = new Random();
        
        //inisiasi kelas TesterAtariPuzzle
        TesterAtariPuzzle demo = new TesterAtariPuzzle();
        
        //variabel tempFit menunjukan keadaan fitness yang sekarang
        int tempFit = -1;
        
        /**
         * tahap preprocessing : untuk mengurangi state space dari puzzle
         * while dilakukan selama fitnessnya tidak berubah sampai tahap preprocessing selesai.
         */
        while (fitness > tempFit) {
            tempFit = fitness;
            // panggil method simpanKotakAngka
            demo.simpanKotakAngka();
            //panggil method reduceSearchSpace
            demo.reduceSearchSpace();
            
            //fitness dihitung berdasarkan banyak bilangan yang ada pada puzzle selain angka 0
            /**
             * array target memiliki beberapa angka yang mengandung makna tertentu.
             * 0 = kotak kosong (sewaktu-waktu bisa disinari / diisi lampu )
             * -1 = kotak yang memiliki lampu
             * 1 = kotak dengan maksimaljumlah lampu yang berada di sekitarnya adalah 1
             * 2 = kotak hitam yang tidak bisa disinari/ diisi lampu/ tidak mengandung angka atau kotak dengan maksimal jumlah lampu yang berada di sekitarnya adalah 2
             * 3 = kotak dengan maksimaljumlah lampu yang berada di sekitarnya adalah 3
             * 4 = kotak dengan maksimal jumlah lampu yang berada di sekitarnya adalah 4
             * 5 = kotak yang disinari lampu
             * 
             */
            fitness = 0;
            for (int i = 0; i < target.length; i++) {
                for (int j = 0; j < target[0].length; j++) {
                    if (target[i][j] != 0) {
                        fitness++;
                    }
                }
            }
        }

        //Initialize population
        /**
         * inisiasi dilakukan dengan menggunakan beberapa parameter
         * 1. jumlah populasi
         * 2. jumlah baris puzzle
         * 3. jumlah kolom puzzle
         * 4. array target yang menjadi panduan pembuatan individu
         */
        demo.population.inisiasipopulasi(10, m, n, target);

        //Calculate fitness of each individual
        demo.population.calculateFitness();

        System.out.println("Generation: " + demo.generationCount + " Fittest: " + demo.population.fittest);

        //While population gets an individual with maximum fitness / akan berhenti jika isi dari puzzle tidak mengandung angka 0
        while (m * n - demo.population.fittest > 0) {
            ++demo.generationCount;

            //Do selection
            demo.selection();

            //Do crossover
            demo.crossover();

            //Do mutation under a random probability
            if (rn.nextInt() % 7 < 5) {
                demo.mutation();
            }

            //Add fittest offspring to population
            demo.addFittestOffspring();

            //Calculate new fitness value
            demo.population.calculateFitness();

            System.out.println("Generation: " + demo.generationCount + " Fittest: " + demo.population.fittest);
        }
        
        //print solusi
        System.out.println("\nSolution found in generation " + demo.generationCount);
        System.out.println("Fitness: " + demo.population.getFittest().fitness);
        System.out.println("Genes: ");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (puzzle[i][j].equalsIgnoreCase("-")) {
                    if (demo.population.getFittest().gen[i][j] == -1) {
                        System.out.print("L" + " ");
                    } else {
                        System.out.print(demo.population.getFittest().gen[i][j] + " ");

                    }
                } else {
                    System.out.print(puzzle[i][j] + " ");
                }

            }
            System.out.println("");
        }

        System.out.println("");

    }
    
    
    /**
     * method ini digunakan untuk
     * array kotakAngka = digunakan untuk menyimpan setiap kotak pada puzzle yang memiliki angka, kecuali angka 0.
     * array probability = digunakan untuk menyimpan jumlah kotak tetangga yang nantinya bisa diisi lampu.
     * array indexI = menyimpan nomor baris dari kotak yang memiliki angka
     * array indexJ = menyimpan nomor kolom dari kotak yang memiliki angka
     * point = sebagai penanda posisi kotakAngka pada array kotakAngka
     */
    void simpanKotakAngka() {
        this.kotakAngka = new ArrayList<>();
        this.probability = new ArrayList<>();
        this.indexI = new ArrayList<>();
        this.IndexJ = new ArrayList<>();
        int point = 0;
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[0].length; j++) {
                if (!puzzle[i][j].equalsIgnoreCase("X") && !puzzle[i][j].equalsIgnoreCase("-") && !puzzle[i][j].equalsIgnoreCase("0")) {
                    indexI.add(i);
                    IndexJ.add(j);
                    int angka = Integer.valueOf(puzzle[i][j]);
                    this.kotakAngka.add(angka);
                    removePossibleSqure();
                    hitungProbabilitas(i, j, point);
                    point++;

                }
            }
        }
        
        System.out.println(Arrays.toString(kotakAngka.toArray()));
        System.out.println(Arrays.toString(probability.toArray()));

    }
    
    /**
     * method ini digunakan untuk menandai kotak disekitar kotak angka yg kemungkinan 
     * kotak tersebut akan diisi lampu kecil, sehingga ditandai saja dengan angka 5 (disinari lampu)
     * 
     */
    void removePossibleSqure() {
        if (!probability.isEmpty()) {
            for (int i = 0; i < probability.size(); i++) {
                if (kotakAngka.get(i) == 0 || probability.get(i) < 1 ) {
                    if (indexI.get(i) - 1 >= 0 && target[indexI.get(i) - 1][IndexJ.get(i)] == 0) {
                        target[indexI.get(i) - 1][IndexJ.get(i)] = 5;
                    }
                    if (indexI.get(i) + 1 < target.length && target[indexI.get(i) + 1][IndexJ.get(i)] == 0) {
                        target[indexI.get(i) + 1][IndexJ.get(i)] = 5;
                    }
                    if (IndexJ.get(i) - 1 >= 0 && target[indexI.get(i)][IndexJ.get(i) - 1] == 0) {
                        target[indexI.get(i)][IndexJ.get(i) - 1] = 5;
                    }
                    if (IndexJ.get(i) + 1 < target.length && target[indexI.get(i)][IndexJ.get(i) + 1] == 0) {
                        target[indexI.get(i)][IndexJ.get(i) + 1] = 5;
                    }
                }
            }
        }

    }
    
    
    /**
     * method ini digunakan untuk menghitung kemungkinan jumlah kotak disekitar kotak angka yang dapat
     * diisi lampu
     */
    void hitungProbabilitas(int i, int j, int point) {
        int probability = 0;
        int temp = 0;

        if (i - 1 >= 0) {
            if (puzzle[i - 1][j].equalsIgnoreCase("-")) {
                if (target[i - 1][j] == 0) {
                    probability++;
                } else if (target[i - 1][j] == -1) {
                    temp = kotakAngka.get(point);
                    temp--;
                    kotakAngka.set(point, temp);
                }

            }
        }

        if (i + 1 < puzzle.length) {
            if (puzzle[i + 1][j].equalsIgnoreCase("-")) {
                if (target[i + 1][j] == 0) {
                    probability++;
                } else if (target[i + 1][j] == -1) {
                    temp = kotakAngka.get(point);
                    temp--;
                    kotakAngka.set(point, temp);
                }

            }
        }

        if (j - 1 >= 0) {
            if (puzzle[i][j - 1].equalsIgnoreCase("-")) {
                if (target[i][j - 1] == 0) {
                    probability++;
                } else if (target[i][j - 1] == -1) {
                    temp = kotakAngka.get(point);
                    temp--;
                    kotakAngka.set(point, temp);
                }

            }

        }

        if (j + 1 < puzzle.length) {
            if (puzzle[i][j + 1].equalsIgnoreCase("-")) {
                if (target[i][j + 1] == 0) {
                    probability++;
                } else if (target[i][j + 1] == -1) {
                    temp = kotakAngka.get(point);
                    temp--;
                    kotakAngka.set(point, temp);
                }
            }
        }

        this.probability.add(probability);
    }
    
    /**
     * method ini digunakan untuk mengurangi state space pada puzzle
     * jika value kotak angka sama dengan jumlah kotak tetangga di sekitar, maka kotak yang menjadi tetangga tersebut akan langsung diisi lampu. 
     */
    void reduceSearchSpace() {
        for (int k = 0; k < kotakAngka.size(); k++) {
            if (kotakAngka.get(k) == probability.get(k)) {

                if (indexI.get(k) - 1 >= 0) {
                    if (target[indexI.get(k) - 1][IndexJ.get(k)] == 0) {
                        target[indexI.get(k) - 1][IndexJ.get(k)] = -1;
                        target = markHorizontalSquare(indexI.get(k) - 1, IndexJ.get(k), target);
                        target = markVerticalSquare(indexI.get(k) - 1, IndexJ.get(k), target);
                    }
                }

                if (indexI.get(k) + 1 < target.length) {
                    if (target[indexI.get(k) + 1][IndexJ.get(k)] == 0) {
                        target[indexI.get(k) + 1][IndexJ.get(k)] = -1;
                        target = markHorizontalSquare(indexI.get(k) + 1, IndexJ.get(k), target);
                        target = markVerticalSquare(indexI.get(k) + 1, IndexJ.get(k), target);
                    }
                }

                if (IndexJ.get(k) - 1 >= 0) {
                    if (target[indexI.get(k)][IndexJ.get(k) - 1] == 0) {
                        target[indexI.get(k)][IndexJ.get(k) - 1] = -1;
                        target = markHorizontalSquare(indexI.get(k), IndexJ.get(k) - 1, target);
                        target = markVerticalSquare(indexI.get(k), IndexJ.get(k) - 1, target);
                    }
                }

                if (IndexJ.get(k) + 1 < target[0].length) {
                    if (target[indexI.get(k)][IndexJ.get(k) + 1] == 0) {
                        target[indexI.get(k)][IndexJ.get(k) + 1] = -1;
                        target = markHorizontalSquare(indexI.get(k), IndexJ.get(k) + 1, target);
                        target = markVerticalSquare(indexI.get(k), IndexJ.get(k) + 1, target);
                    }
                }
            }
        }

    }
    
    /**
     * method ini digunakan untuk menerangi kotak secara horizontal
     */

    int[][] markHorizontalSquare(int i, int j, int[][] target) {
        int jleft = j - 1;
        int jright = j + 1;
        while (jleft >= 0 && (target[i][jleft] == 0 || target[i][jleft] == 5) && target[i][jleft] != 2) {
            target[i][jleft] = 5;
            jleft--;
        }

        while (jright < target.length && (target[i][jright] == 0 || target[i][jright] == 5) && target[i][jright] != 2) {
            target[i][jright] = 5;
            jright++;
        }

        return target;
    }
    
     /**
     * method ini digunakan untuk menerangi kotak secara vertikal
     */
    int[][] markVerticalSquare(int i, int j, int[][] target) {
        int iUp = i - 1;
        int iDown = i + 1;
        while (iUp >= 0 && (target[iUp][j] == 5 || target[iUp][j] == 0) && target[iUp][j] != 2) {
            target[iUp][j] = 5;
            iUp--;
        }

        while (iDown < target.length && (target[iDown][j] == 5 || target[iDown][j] == 0) && target[iDown][j] != 2) {
            target[iDown][j] = 5;
            iDown++;
        }

        return target;
    }

    //Selection
    void selection() {

        //Select the most fittest individual
        fittest = population.getFittest();

        //Select the second most fittest individual
        secondFittest = population.getSecondFittest();

    }

    //Crossover
    void crossover() {
        Random rn = new Random();

        //Select a random crossover point
        int R = (int) Math.round(Math.random()); //random bilangan 1 atau 0,
        //Rrow dan Rcol adalah bilangan random yang mengindikasikan baris dan kolom untuk dilakukan crossover
        int Rrow = rn.nextInt(m); //random range 1 <= i <= m (banyak baris)
        int Rcol = rn.nextInt(n); //random range 1 <= i <= n (banyak kolom)

        if (R > 0.5) {
            for (int i = 0; i < m; i++) {
                if (i < Rrow) {
                    for (int j = 0; j < n; j++) {
                        if (j >= 1 && j <= n) {
                            int temp = fittest.gen[i][j];
                            fittest.gen[i][j] = secondFittest.gen[i][j];
                            secondFittest.gen[i][j] = temp;
                        }
                    }
                } else if (i == Rrow) {
                    for (int j = 0; j < n; j++) {
                        if (j >= 1 && j <= Rcol) {
                            int temp = fittest.gen[i][j];
                            fittest.gen[i][j] = secondFittest.gen[i][j];
                            secondFittest.gen[i][j] = temp;
                        }
                    }
                } else if (i > Rrow) {
                    for (int j = 0; j < n; j++) {
                        if (j >= 1 && j <= n) {
                            int temp = fittest.gen[i][j];
                            fittest.gen[i][j] = secondFittest.gen[i][j];
                            secondFittest.gen[i][j] = temp;
                        }
                    }
                }

            }
        } else {
            for (int i = 0; i < n; i++) {
                if (i < Rcol) {
                    for (int j = 0; j < m; j++) {
                        if (j >= 1 && j <= m) {
                            int temp = fittest.gen[j][i];
                            fittest.gen[j][i] = secondFittest.gen[j][i];
                            secondFittest.gen[j][i] = temp;
                        }
                    }
                } else if (i == Rcol) {
                    for (int j = 0; j < m; j++) {
                        if (j >= 1 && j <= Rrow) {
                            int temp = fittest.gen[j][i];
                            fittest.gen[j][i] = secondFittest.gen[j][i];
                            secondFittest.gen[j][i] = temp;
                        }
                    }
                } else if (i > Rcol) {
                    for (int j = 0; j < m; j++) {
                        if (j >= 1 && j <= m) {
                            int temp = fittest.gen[j][i];
                            fittest.gen[j][i] = secondFittest.gen[j][i];
                            secondFittest.gen[j][i] = temp;
                        }
                    }
                }

            }
        }
    }

    //Mutation
    void mutation() {
        Random rn = new Random();

        //Select a random mutation point
        int R = (int) Math.round(Math.random()); //random bilangan 1 atau 0,
        //Rrow dan Rcol adalah bilangan random yang mengindikasikan baris dan kolom untuk dilakukan mutasi
        int Rrow = rn.nextInt(m); //random range 1 <= i <= m (banyak baris)
        int Rcol = rn.nextInt(n); //random range 1 <= i <= n (banyak kolom)

        //Select a random mutation point
        //int mutationPoint = rn.nextInt(population.individuals[0].geneLength);
        //Flip values at the mutation point
        if (target[Rrow][Rcol] == 0) {
            if (fittest.gen[Rrow][Rcol] == 0) {
                fittest.gen[Rrow][Rcol] = -1;
                fittest.gen = markHorizontalSquare(Rrow, Rcol, fittest.gen);
                fittest.gen = markVerticalSquare(Rrow, Rcol, fittest.gen);

            } else if(fittest.gen[Rrow][Rcol] == -1) {
                fittest.gen[Rrow][Rcol] = 0;
            }
        }

        //mutationPoint = rn.nextInt(population.individuals[0].geneLength);
        Rrow = rn.nextInt(m); //random range 1 <= i <= m (banyak baris)
        Rcol = rn.nextInt(n); //random range 1 <= i <= n (banyak kolom)
        if (target[Rrow][Rcol] == 0) {
            if (secondFittest.gen[Rrow][Rcol] == 0) {
                secondFittest.gen[Rrow][Rcol] = -1;
                secondFittest.gen = markHorizontalSquare(Rrow, Rcol, secondFittest.gen);
                secondFittest.gen = markVerticalSquare(Rrow, Rcol, secondFittest.gen);

            } else if (secondFittest.gen[Rrow][Rcol] == -1) {
                secondFittest.gen[Rrow][Rcol] = 0;
            }
        }

    }

    //Get fittest offspring
    IndividuPuzzle getFittestOffspring() {
        if (fittest.fitness > secondFittest.fitness) {
            return fittest;
        }
        return secondFittest;
    }

    //Replace least fittest individual from most fittest offspring
    void addFittestOffspring() {

        //Update fitness values of offspring
        fittest.calcFitness();
        secondFittest.calcFitness();

        //Get index of least fit individual
        int leastFittestIndex = population.getLeastFittestIndex();

        //Replace least fittest individual from most fittest offspring
        population.individuals[leastFittestIndex] = getFittestOffspring();
    }

//membaca inputan dari file txt
    static boolean parse(BufferedReader in) {
        puzzle = null;
        try {
            String line;
            //selama inputnya bukan merupakan 'begin'
            do {
                line = in.readLine();
                //jika input bernilai false, maka berhenti membaca input
                if (line == null) {
                    return false;
                }
            } while (!line.equals("begin"));

            line = in.readLine();
            if (line == null) {
                return false;
            }
            String[] tokens = line.split("\\s+");

            //set ukuran dari puzzle
            if (tokens.length != 3 || !tokens[0].equals("size")) {
                return false;
            }
            m = Integer.parseInt(tokens[1]);
            n = Integer.parseInt(tokens[2]);

            if (m <= 0 || n <= 0) {
                return false;
            }

            //simpan setiap value pada input ke puzzle
            puzzle = new String[m][];
            for (int i = 0; i < m; i++) {
                line = in.readLine();
                puzzle[i] = line.split("\\s+");
                if (puzzle.length != n) {
                    return false;
                }
            }
            line = in.readLine();

            //jika false berhenti membaca input
            if (!line.equals("end")) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;

    }

}

//Individual class
class IndividuPuzzle {

    int fitness = 0;
    int[][] target;
    int m;
    int n;
    int[][] gen;

    public IndividuPuzzle(int m, int n, int[][] target) {
        Random rn = new Random();
        this.m = m;
        this.n = n;
        this.target = target;
        gen = new int[m][n];
        //Set genes randomly for each individual        
        for (int i = 0; i < this.m; i++) {
            for (int j = 0; j < this.n; j++) {
                if (target[i][j] != -1 && target[i][j] != 2 && target[i][j] != 5) {
                    gen[i][j] = (int) Math.round(Math.random()) - 1;
                } else {
                    gen[i][j] = target[i][j];
                }
            }
        }

        fitness = 0;
    }

    //Calculate fitness
    public void calcFitness() {

        fitness = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (gen[i][j] != 0) {
                    fitness++;
                }

            }
        }
    }

}

class PopulasiPuzzle {

    IndividuPuzzle[] individuals;
    int fittest = 0;

    //inisiasi populasi
    public void inisiasipopulasi(int size, int m, int n, int[][] target) {
        individuals = new IndividuPuzzle[size];

        for (int i = 0; i < size; i++) {
            individuals[i] = new IndividuPuzzle(m, n, target);
        }

    }

    //get Fittest
    public IndividuPuzzle getFittest() {
        int maxFit = Integer.MIN_VALUE;
        int maxFitIndex = 0;

        for (int i = 0; i < individuals.length; i++) {
            if (maxFit <= individuals[i].fitness) {
                maxFit = individuals[i].fitness;
                maxFitIndex = i;
            }
        }

        fittest = individuals[maxFitIndex].fitness;
        return individuals[maxFitIndex];
    }

    //Get the second most fittest individual
    public IndividuPuzzle getSecondFittest() {
        int maxFit1 = 0;
        int maxFit2 = 0;
        for (int i = 0; i < individuals.length; i++) {
            if (individuals[i].fitness > individuals[maxFit1].fitness) {
                maxFit2 = maxFit1;
                maxFit1 = i;
            } else if (individuals[i].fitness > individuals[maxFit2].fitness) {
                maxFit2 = i;
            }
        }
        return individuals[maxFit2];
    }

    //Get index of least fittest individual
    public int getLeastFittestIndex() {
        int minFitVal = Integer.MAX_VALUE;
        int minFitIndex = 0;
        for (int i = 0; i < individuals.length; i++) {
            if (minFitVal >= individuals[i].fitness) {
                minFitVal = individuals[i].fitness;
                minFitIndex = i;
            }
        }
        return minFitIndex;
    }

    //Calculate fitness of each individual
    public void calculateFitness() {

        for (int i = 0; i < individuals.length; i++) {
            individuals[i].calcFitness();
        }
        getFittest();
    }

}

