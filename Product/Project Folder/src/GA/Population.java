package GA;

import functional.Room;
import functional.StudentString;
import controllers.main.MainController;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Population {

    public DNA getBestOne() {
        return bestOne;
    }

    private DNA bestOne;
    private int populationNum;
    private double mutationRate;
    private DNA[] population;
    private int generationCount = 1;
    private int sumFitness;
    private int counterForStop = 0;
    private DNA[] nextGeneration;
    private List<Integer> bestFitnesses = new ArrayList<>();
    private List<Room> fixedGenes;
    private int stoppingCondition;

    public List<StudentString> getUnallocatedStudents() {
        return unallocatedStudents;
    }

    private List<StudentString> unallocatedStudents;

    public Population(int populationNum, double mutationRate, int stoppingCondition, List<Room> fixedGenes, int year) {
        this.unallocatedStudents = findUnallocatedStudents(year);
        this.stoppingCondition = stoppingCondition;
        this.fixedGenes = fixedGenes;
        this.populationNum = populationNum;
        this.mutationRate = mutationRate;
        population = new DNA[populationNum];
        System.out.println("Generation " + generationCount);
        System.out.println("------------------------------");
        for (int i = 0; i < populationNum; i++) {
            this.population[i] = new DNA(unallocatedStudents, fixedGenes);
        }
    }

    public void naturalSelection() {
        Random random = new Random();
        nextGeneration = new DNA[populationNum];
        nextGeneration[0] = new DNA(bestOne, fixedGenes);
        for (int i = 1; i < populationNum; i++) {
            DNA parent;
            int randomFitness = random.nextInt(sumFitness);
            int index = -1;
            while (randomFitness >= 0) {
                randomFitness -= population[++index].getFitness();
            }
            parent = population[index];
            DNA child = parent.mutate(mutationRate);
            nextGeneration[i] = child;
        }
        System.arraycopy(nextGeneration,0,population,0,populationNum);
        generationCount++;
        System.out.println("Generation " + generationCount);
        System.out.println("------------------------------");
    }


    public void calcFitness() {
        int bestFitness = 0;
        sumFitness = 0;
        for (int i = 0; i < populationNum; i++) {
            int currentFitness = population[i].calcFitness();
            if (currentFitness > bestFitness) {
                bestOne = population[i];
                bestFitness = currentFitness;
            }
            sumFitness += currentFitness;
        }
        System.out.println(bestOne.getFitness() + "\n" + bestOne.getFitnessFactor() + "\n" + bestOne.getDeviationCount());
    }

    public boolean evaluate() {
        bestFitnesses.add(bestOne.getFitness());
        int size = bestFitnesses.size();
        if (size > 1) {
            if (bestFitnesses.get(size-1).equals(bestFitnesses.get(size-2))) {
                counterForStop++;
            }
            else {
                counterForStop = 0;
            }
        }
        return counterForStop <= stoppingCondition;
    }

    public static List<StudentString> findUnallocatedStudents(int year) {
        List<StudentString> unallocatedStudents = new ArrayList<>();
        try {
            Statement stmt = MainController.c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Rooms;");
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            List<Integer> allocatedStudentIds = new ArrayList<>();
            while (rs.next()) {
                for (int i = 6; i <= numberOfColumns; i++) {
                    int studentId = rs.getInt(i);
                    if (studentId != 0)
                        allocatedStudentIds.add(studentId);
                }
            }
            ResultSet rs1 = stmt.executeQuery("SELECT Id FROM Students;");
            List<Integer> allStudentIds = new ArrayList<>();

            while (rs1.next()) {
                allStudentIds.add(rs1.getInt(1));
            }
            for (Integer allocatedStudentId: allocatedStudentIds) {
                allStudentIds.remove(allocatedStudentId);
            }
            List<Integer> unallocatedStudentIds = allStudentIds;
            for (Integer studentId: unallocatedStudentIds) {
                StudentString student = new StudentString();
                ResultSet rs2 = stmt.executeQuery("SELECT * FROM Students WHERE Id = " + studentId + ";");
                rs2.next();
                int studentYear = rs2.getInt("Year");
                if (year != 3 && studentYear != year) {
                    continue;
                }
                student.setId(studentId);
                student.setGivenName(rs2.getString("GivenName"));
                student.setFamilyName(rs2.getString("FamilyName"));
                student.setSex(rs2.getString("Sex"));
                student.setCountry(rs2.getString("Country"));
                student.setContinent(rs2.getString("Continent"));
                student.setYear(studentYear);
                unallocatedStudents.add(student);
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unallocatedStudents;
    }
}
