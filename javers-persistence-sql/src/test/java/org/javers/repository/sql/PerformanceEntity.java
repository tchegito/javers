package org.javers.repository.sql;

import org.javers.core.metamodel.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * class for performance tests
 * @author bartosz walacik
 */
class PerformanceEntity {

    @Id
    int id;

    List<PerformanceEntity> refs = new ArrayList<>();

    List<Integer> intList;

    public PerformanceEntity generateRandomList(int count) {
        intList = new ArrayList<>();
        Random random = new Random();
        for (int i=0; i< count; i++) {
            intList.add(i, random.nextInt());
        }
        return this;
    }

    public PerformanceEntity changeAll() {
        Random random = new Random();
        for (int i=0; i< intList.size(); i++) {
            intList.set(i, random.nextInt());
        }
        return this;
    }

    public PerformanceEntity removeElement(int index) {
        intList.remove(index);
        return this;
    }
}
