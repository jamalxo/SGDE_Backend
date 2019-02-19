package de.subhan.sgd;

import ch.qos.logback.core.net.SyslogOutputStream;
import de.subhan.sgd.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@RestController
public class Controller {

    @Qualifier("Classificator0")
    @Autowired
    Classificator classificator0;

    @Qualifier("Classificator1")
    @Autowired
    Classificator classificator1;

    @Qualifier("Classificator2")
    @Autowired
    Classificator classificator2;

    @Qualifier("Classificator3")
    @Autowired
    Classificator classificator3;

    @Qualifier("Classificator4")
    @Autowired
    Classificator classificator4;

    @Qualifier("Classificator5")
    @Autowired
    Classificator classificator5;

    @Qualifier("Classificator6")
    @Autowired
    Classificator classificator6;

    @Qualifier("Classificator7")
    @Autowired
    Classificator classificator7;

    @Qualifier("Classificator8")
    @Autowired
    Classificator classificator8;

    @Qualifier("Classificator9")
    @Autowired
    Classificator classificator9;



    @PostMapping(path = "getNumber")
    public ServiceResponse index(
            @RequestBody ServiceRequest request
    ) {
        //set labels
        classificator0.setLabel(0);
        classificator1.setLabel(1);
        classificator2.setLabel(2);
        classificator3.setLabel(3);
        classificator4.setLabel(4);
        classificator5.setLabel(5);
        classificator6.setLabel(6);
        classificator7.setLabel(7);
        classificator8.setLabel(8);
        classificator9.setLabel(9);

        //set instances
        classificator0.setInstances(4152);
        classificator1.setInstances(4714);
        classificator2.setInstances(4212);
        classificator3.setInstances(4243);
        classificator4.setInstances(4090);
        classificator5.setInstances(3832);
        classificator6.setInstances(4143);
        classificator7.setInstances(4360);
        classificator8.setInstances(4117);
        classificator9.setInstances(4138);


        List<Double> point = request.getPoint();

        // check if request is viable
        if(point.size() != 784){
            return new ServiceResponse(10);
        } else{
            System.out.println("Calculating...");
        }

        List<Classificator> classificatorList = new ArrayList<Classificator>();

        classificatorList.add(classificator0);
        classificatorList.add(classificator1);
        classificatorList.add(classificator2);
        classificatorList.add(classificator3);
        classificatorList.add(classificator4);
        classificatorList.add(classificator5);
        classificatorList.add(classificator6);
        classificatorList.add(classificator7);
        classificatorList.add(classificator8);
        classificatorList.add(classificator9);

        Evaluator prediction = evalClassificator(classificatorList, point);


        return new ServiceResponse(prediction.getLabel());
    }


    private Evaluator evalClassificator(List<Classificator> classificatorList, List<Double> point) {
        List<Evaluator> evaluatorList = new ArrayList<Evaluator>();

        //get all numinstances
        int sumInstances = 0;
        for (int i = 0; i < classificatorList.size(); i++){
            sumInstances += classificatorList.get(i).getInstances();
        }

        for(int i = 0; i < classificatorList.size(); i++){
            Evaluator tmp = new Evaluator();
            double classDensity = evalSparseGrid(classificatorList.get(i), point);
            double prior = (double) classificatorList.get(i).getInstances() /sumInstances;
            double density = prior * classDensity;
            tmp.setValue(density);
            tmp.setLabel(i);
            evaluatorList.add(tmp);
        }

        Evaluator result = Collections.max(evaluatorList, Comparator.comparing(e -> e.getValue()));

        return result;
    }

    private Double evalSparseGrid(Classificator classificator, List<Double> point) {
        Double sum = 0.0;
        Double product = 0.0;

        Grid grid = classificator.getGrid();
        List<Double> surpluses = grid.getSurpluses();
        List<List<GridPoint>> gridPoints = grid.getGridPoints();

        for(int i = 0; i < gridPoints.size(); i++){
            for(int j = 0; j < gridPoints.get(i).size(); j++){
                if(j==0){
                    product = calcModifiedLinear(gridPoints.get(i).get(j).getLevel(), gridPoints.get(i).get(j).getIndex(), point.get(j));
                } else{
                    product = product * calcModifiedLinear(gridPoints.get(i).get(j).getLevel(), gridPoints.get(i).get(j).getIndex(), point.get(j));
                }
            }
            sum += product * surpluses.get(i);
        }
        return sum;
    }

    private double calcModifiedLinear(int level, int index, double x){
        if(level == 1 && index == 1){
            return 1.0;
        } else if(level > 1 && index ==1){
            if(0 <= x && x <= (1)/(Math.pow(2,level-1))){
                return 2 - (Math.pow(2,level) * x);
            } else{
                return 0.0;
            }
        } else if(level > 1 && index == (Math.pow(2,level))-1){
            if(1-((1)/(Math.pow(2, level-1))) <= x && x <= 1){
                return Math.pow(2, level) * x + 1 - index;
            } else{
                return 0.0;
            }
        } else{
            return calcLinearN(level, index, x);
        }
    }

    private double calcLinearN(int level, int index, double x){
        double value = (Math.pow(2,level) * x) -1;
        return calcLinearOne(value);
    }

    private double calcLinearOne(double x){
        double valueOne = 1 - Math.abs(x);
        double valueTwo = 0;
        return Math.max(valueOne, valueTwo);
    }
}