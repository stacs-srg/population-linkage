/*
 * Copyright 2022 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population-linkage.
 *
 * population-linkage is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population-linkage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population-linkage. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.population_linkage.aleks;

import uk.ac.standrews.cs.neoStorr.impl.LXP;
import uk.ac.standrews.cs.neoStorr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.neoStorr.interfaces.IBucket;
import uk.ac.standrews.cs.population_records.RecordRepository;
import uk.ac.standrews.cs.population_records.record_types.Birth;
import uk.ac.standrews.cs.population_records.record_types.Death;

import java.time.LocalDate;
import java.util.*;

/**
 * Used to encode unmatched triangles with nodes x and y and z
 * x and y are xy_distance apart, y and z are yz_distance apart
 * but xz is not connected.
 * All the ids are storr ids of Nodes.
 */
public class OpenTriangleCluster {
    public final long x;
    public List<List<Long>> triangleChain = new ArrayList<>();
    private Set<LXP> children = new HashSet<LXP>();
    Map<String, Integer> birthplaceMap = new HashMap<String, Integer>();
    private List<LocalDate> birthDays = new ArrayList<>();
    private double yearTotal = 0;
    private int ageRange;
    private double yearAvg;
    private int yearMedian;
    private String mostCommonBirthplace = null;

    IBucket births;
    IBucket deaths;

    public OpenTriangleCluster(long x, List<List<Long>>  triangleChain) {
        RecordRepository record_repository = new RecordRepository("umea");
        births = record_repository.getBucket("birth_records");
        deaths = record_repository.getBucket("death_records");
        this.x = x;
        this.triangleChain = triangleChain;
    }

    public String toString() {
        return "X = " + x;
    }

    public List<List<Long>> getTriangleChain() {
        return triangleChain;
    }

    public void getYearStatistics() throws BucketException {
        for (List<Long> chain : triangleChain){
            LXP[] tempKids = {(LXP) births.getObjectById(x), (LXP) deaths.getObjectById(chain.get(0)), (LXP) births.getObjectById(chain.get(1))};
            for (int i = 0; i < tempKids.length; i++) {
                if (!children.contains(tempKids[i])) {
                    int year = 1850;
                    int month = 1;
                    int day = 1;
                    if(i != 1){
                        try{
                            year = Integer.parseInt(tempKids[i].getString(Birth.BIRTH_YEAR));
                            month = Integer.parseInt(tempKids[i].getString(Birth.BIRTH_MONTH));
                            day = Integer.parseInt(tempKids[i].getString(Birth.BIRTH_DAY));
                        }catch(Exception e){
                            if(!children.isEmpty()){
                                year = (int) Math.round(yearTotal/children.size());
                            }
                        }

                        if(!Objects.equals(tempKids[i].getString(Birth.BIRTH_ADDRESS), "----")){
                            birthplaceMap.merge(tempKids[i].getString(Birth.BIRTH_ADDRESS), 1, Integer::sum);
                        }
                    }else{
                        try{
                            year = Integer.parseInt((tempKids[i].getString(Death.DATE_OF_BIRTH)).substring(6));
                            month = Integer.parseInt((tempKids[i].getString(Death.DATE_OF_BIRTH)).substring(3, 5));
                            day = Integer.parseInt((tempKids[i].getString(Death.DATE_OF_BIRTH)).substring(0, 2));
                        }catch(Exception e){
                            if(!children.isEmpty()){
                                year = (int) Math.round(yearTotal/children.size());
                            }
                        }
                    }
                    birthDays.add(LocalDate.of(year, month, day));
                    yearTotal += year;
                }
                children.add(tempKids[i]);
            }
        }

        //https://deveshsharmablogs.wordpress.com/2013/07/16/find-most-common-element-in-a-list-in-java/
        int maxValue = -1;
        for(Map.Entry<String, Integer> entry: birthplaceMap.entrySet()) {
            if(entry.getValue() > maxValue) {
                mostCommonBirthplace = entry.getKey();
                maxValue = entry.getValue();
            }
        }

        Collections.sort(birthDays);
        ageRange = birthDays.get(birthDays.size() - 1).getYear() - birthDays.get(0).getYear();
        yearAvg = yearTotal / children.size();
        if ((birthDays.size() % 2) == 0) {
            yearMedian = ((birthDays.get(birthDays.size() / 2)).getYear() + (birthDays.get(birthDays.size() / 2 - 1)).getYear()) / 2;
        }else {
            yearMedian = birthDays.get(birthDays.size() / 2).getYear();
        }
    }

    public void removeChain(List<Long> chain) {
        triangleChain.remove(chain);
    }

    public int getAgeRange() {
        return ageRange;
    }

    public double getYearAvg() {
        return yearAvg;
    }

    public double getNumOfChildren() {
        return children.size();
    }

    public List<LocalDate> getBirthDays() {
        return birthDays;
    }

    public double getYearMedian() {
        return yearMedian;
    }

    public String getMostCommonBirthplace() {
        return mostCommonBirthplace;
    }
}
