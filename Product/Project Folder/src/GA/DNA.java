package GA;

import functional.Room;
import functional.StudentString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DNA {
    private List<StudentString> unallocatedStudents;
    private List<Room> genes;
    private int fitness;

    public boolean isNotValid() {
        return notValid;
    }

    private boolean notValid = false;

    public double getDeviationCount() {
        return deviationCount;
    }

    private double deviationCount;
    private List<Room> fixedGenes;
    private double fitnessFactor;

    public double getFitnessFactor() {
        return fitnessFactor;
    }

    DNA(DNA dna, List<Room> fixedGenes) {
        this.genes = deepCopyRooms(dna.getGenes());
        this.fixedGenes = deepCopyRooms(fixedGenes);
    }

    DNA(List<StudentString> unallocatedStudents, List<Room> fixedGenes) {
        this.unallocatedStudents = unallocatedStudents;
        this.fixedGenes = deepCopyRooms(fixedGenes);
        this.genes = deepCopyRooms(fixedGenes);
        buildDNA();
    }

    private void buildDNA() {
        Random random = new Random();
        for (StudentString unallocatedStudent: unallocatedStudents) {
            String sexRoom = "";
            Room gene;
            int bedsAvailable;
            do {
                gene = genes.get(random.nextInt(genes.size()));
                if (gene.getSexRoom().equals("Boy")) sexRoom = "male";
                if (gene.getSexRoom().equals("Girl")) sexRoom = "female";
                bedsAvailable = gene.getMaxResidents() - gene.getStudents().size();
            } while (!sexRoom.equals(unallocatedStudent.getSex()) || bedsAvailable == 0);
            gene.getStudents().add(unallocatedStudent);
        }
    }

    public List<Room> getGenes() {
        return genes;
    }

    public int getFitness() {
        return fitness;
    }

    public int calcFitness() {
        int fitness = 0;
        int mode;
        List<Integer> roomsStudents = new ArrayList<>();
        for (Room gene: genes) {
            roomsStudents.add(gene.getStudents().size());
        }
        mode = getMode(roomsStudents);
        deviationCount = 0;
        for (Room gene: genes) {
            deviationCount += Math.abs(gene.getStudents().size() - mode);
        }
        for (Room gene: genes) {
            List<StudentString> students = gene.getStudents();
            List<String> countries = new ArrayList<>();
            List<String> continents = new ArrayList<>();
            for (StudentString student: students) {
                countries.add(student.getCountry());
                continents.add(student.getContinent());
            }
            Collections.sort(countries);
            Collections.sort(continents);
            int continentConflicts = 0;
            int countryConflicts = 0;
            for (int i = 1; i < countries.size(); i++) {
                if (countries.get(i-1).equals(countries.get(i))) {
                    countryConflicts++;
                }
            }
            for (int i = 1; i < continents.size(); i++) {
                if (continents.get(i-1).equals(continents.get(i))) {
                    continentConflicts++;
                }
            }
            if (countryConflicts == 0 && continentConflicts == 0) {fitness += 9;}
            else if (countryConflicts == 0 && continentConflicts == 1) {fitness += 8;}
            else if (countryConflicts == 1 && continentConflicts == 1 && (gene.getStudents().size() == mode)) {fitness += 7;}
            else if (countryConflicts == 1 && continentConflicts == 1 && (gene.getStudents().size() < mode)) {fitness += 6;}
            else if (countryConflicts == 0 && continentConflicts == 2 && (gene.getStudents().size() == mode)) {fitness += 6;}
            else if (countryConflicts == 0 && continentConflicts == 2 && (gene.getStudents().size() < mode)) {fitness += 5;}
            else if (countryConflicts == 1 && continentConflicts == 2 && (gene.getStudents().size() == mode)) {fitness += 5;}
            else if (countryConflicts == 1 && continentConflicts == 2 && (gene.getStudents().size() < mode)) {fitness += 4;}
        }
        this.fitnessFactor = 1 / (1 + Math.pow(2.7, 0.05 * (deviationCount - 35)));
        this.fitness = Math.round((float) fitness * (float) fitness * (float) fitnessFactor);
        return this.fitness;
    }

    public DNA mutate(double mutationRate) {
        Random random = new Random();
        if (random.nextDouble() < mutationRate) {
            if (random.nextDouble() < 0.3) {
                int maxStudents = genes.get(0).getStudents().size();
                int minStudents = genes.get(0).getStudents().size();
                for (Room gene: genes) {
                    if (gene.getStudents().size() > maxStudents) maxStudents = gene.getStudents().size();
                    else if (gene.getStudents().size() < minStudents) minStudents = gene.getStudents().size();
                }
                List<Room> maxStudentBoysRooms = new ArrayList<>();
                List<Room> maxStudentGirlsRooms = new ArrayList<>();
                List<Room> minStudentBoysRooms = new ArrayList<>();
                List<Room> minStudentGirlsRooms = new ArrayList<>();
                for (Room gene: genes) {
                    if (gene.getStudents().size() == maxStudents && gene.getSexRoom().equals("Boy")) maxStudentBoysRooms.add(gene);
                    else if (gene.getStudents().size() == maxStudents && gene.getSexRoom().equals("Girl")) maxStudentGirlsRooms.add(gene);
                    else if (gene.getStudents().size() == minStudents && gene.getSexRoom().equals("Boy")) minStudentBoysRooms.add(gene);
                    else if (gene.getStudents().size() == minStudents && gene.getSexRoom().equals("Girl")) minStudentGirlsRooms.add(gene);
                }
                exchangeRoom(random, maxStudentBoysRooms, minStudentBoysRooms);
                exchangeRoom(random, maxStudentGirlsRooms, minStudentGirlsRooms);
            } else {
                Room room1;
                Room room2;
                do {
                    room1 = genes.get(random.nextInt(genes.size()));
                } while (room1.getStudents().size() == 0);
                do {
                    room2 = genes.get(random.nextInt(genes.size()));
                } while (room2 == room1 || !room2.getSexRoom().equals(room1.getSexRoom()) || room2.getStudents().size() == 0);
                StudentString student1 = null;
                StudentString student2 = null;
                int count = 0;
                do {
                    student1 = room1.getStudents().get(random.nextInt(room1.getStudents().size()));
                    count++;
                } while (count < 50 && fixedGenes.get(getIndex(room1)).getStudents().contains(student1));
                if (count == 50) {
                    student1 = null;
                } else {
                    count = 0;
                    do {
                        student2 = room2.getStudents().get(random.nextInt(room2.getStudents().size()));
                        count++;
                    } while (count < 50 && fixedGenes.get(getIndex(room2)).getStudents().contains(student2));
                    if (count == 50) student2 = null;
                }
                if (student1 != null && student2 != null) {
                    room1.getStudents().remove(student1);
                    room2.getStudents().remove(student2);
                    room1.getStudents().add(student2);
                    room2.getStudents().add(student1);
                }
            }

            /*}*/
            /*List<Room> modeRooms = new ArrayList<>();
            List<Room> deviatedRooms = new ArrayList<>();
            for (Room gene : genes) {
                if (Math.abs(gene.getStudents().size() - mode) >= 1)
                    deviatedRooms.add(gene);
                else if (gene.getStudents().size() == mode)
                    modeRooms.add(gene);
            }
            if (deviatedRooms.size() > 0) {
                Room room1 = deviatedRooms.get(random.nextInt(deviatedRooms.size()));
                Room room2;
                do {
                    room2 = modeRooms.get(random.nextInt(modeRooms.size()));
                } while (!room2.getSexRoom().equals(room1.getSexRoom()));
                StudentString student;
                int count = 0;
                if (room1.getStudents().size() < room2.getStudents().size()) {
                    do {
                        student = room2.getStudents().get(random.nextInt(room2.getStudents().size()));
                        count++;
                    } while (count < 50 && fixedGenes.get(getIndex(room2)).getStudents().contains(student));
                    if (count < 50){
                        room1.getStudents().add(student);
                    }
                } else {
                    do {
                        student = room1.getStudents().get(random.nextInt(room1.getStudents().size()));
                        count++;
                    } while (count < 50 && fixedGenes.get(getIndex(room1)).getStudents().contains(student));
                    if (count < 50){
                        room2.getStudents().add(student);
                    }
                }
            } else {

            }*/
        }
        return new DNA(this, this.fixedGenes);
    }

    private void exchangeRoom(Random random, List<Room> maxStudentRooms, List<Room> minStudentRooms) {
        if (!maxStudentRooms.isEmpty() && !minStudentRooms.isEmpty()) {
            Room roomMore = maxStudentRooms.get(random.nextInt(maxStudentRooms.size()));
            Room roomLess = minStudentRooms.get(random.nextInt(minStudentRooms.size()));
            StudentString student;
            int count = 0;
            do {
                student = roomMore.getStudents().get(random.nextInt(roomMore.getStudents().size()));
                count++;
            } while (count < 50 && fixedGenes.get(getIndex(roomMore)).getStudents().contains(student));
            if (count < 50){
                roomLess.getStudents().add(student);
                roomMore.getStudents().remove(student);
            }
        }
    }


    public int getMode(List<Integer> list) {
        int mode = list.get(0);
        int maxCount = 0;
        for (int i = 0; i < list.size(); i++) {
            int value = list.get(i);
            int count = 0;
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j) == value) count++;
                if (count > maxCount) {
                    mode = value;
                    maxCount = count;
                }
            }
        }
        if (maxCount > 1) {
            return mode;
        }
        return 0;
    }

    public static List<Room> deepCopyRooms(List<Room> rooms) {
        List<Room> newRooms = new ArrayList<>();
        for (Room room: rooms) {
            newRooms.add(new Room(room));
        }
        return newRooms;
    }

    private int getIndex(Room room) {
        int index = -1;
        for (int i = 0; i < genes.size(); i++) {
            if (room.equals(genes.get(i)))
                index = i;
        }
        return index;
    }
}
