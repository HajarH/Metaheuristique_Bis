package jobshop.encodings;

import java.util.ArrayList;

public class TaskTime{
    public Task t;
    public int job;
    public int startT;
    public int endT;
    public int durationT;

    public TaskTime(Task t, int start, int end, int dur){
        this.t = t;
        this.job = this.t.job;
        this.startT =start;
        this.durationT = dur;
        this.endT = end;
    }

    public static ArrayList<TaskTime> OrderByStartTime(ArrayList<TaskTime> list){
        ArrayList<TaskTime> res = new ArrayList<TaskTime>();
        ArrayList<TaskTime> temp = new ArrayList<TaskTime>();
        int aux;
        TaskTime auxT;
        for(int i=0; i<list.size(); i++){
            temp.add(new TaskTime(list.get(i).t, list.get(i).startT, list.get(i).endT, list.get(i).durationT));
        }

        for(int u=0; u<temp.size();u++){
            for(int j=0; j< temp.size();j++){
                aux =temp.get(j).startT;
                auxT = temp.get(j);
                for(int i = 0; i< temp.size(); i++){
                    if(aux>temp.get(i).startT){
                        aux = temp.get(i).startT;
                        auxT = temp.get(i);
                    }
                }
                res.add(new TaskTime(auxT.t,auxT.startT,auxT.endT,auxT.durationT));
                temp.remove(auxT);
            }
        }
        res.add(temp.get(0));

        return res;
    }

    public static ArrayList<TaskTime> OrderSPT(ArrayList<TaskTime> list){
        ArrayList<TaskTime> res = new ArrayList<TaskTime>();
        ArrayList<TaskTime> temp = new ArrayList<TaskTime>();
        int aux;
        TaskTime auxT;
        for(int i=0; i<list.size(); i++){
            temp.add(new TaskTime(list.get(i).t, list.get(i).startT, list.get(i).endT, list.get(i).durationT));
        }
        for(int u=0; u<temp.size();u++){
            for(int j=0; j< temp.size();j++){
                aux =temp.get(j).durationT;
                auxT = temp.get(j);
                for(int i = 0; i< temp.size(); i++){
                    if(aux>temp.get(i).durationT){
                        aux = temp.get(i).durationT;
                        auxT = temp.get(i);
                    }
                }
                res.add(new TaskTime(auxT.t,auxT.startT,auxT.endT,auxT.durationT));
                temp.remove(auxT);
            }
        }
        res.add(temp.get(0));

        return res;
    }

    public static ArrayList<Task> toTask(ArrayList<TaskTime> list){
        ArrayList<Task> res = new ArrayList<Task>();
        for(int i = 0; i<list.size(); i++){
            res.add(list.get(i).t);
        }
        return res;
    }

    public static Task toTask(TaskTime tt){
        return tt.t;
    }

    @Override
    public String toString(){
        return this.t.toString()+" j= "+this.t.job+" s= "+this.startT+" e= "+this.endT+" d= "+this.durationT +"\n";
    }
}
