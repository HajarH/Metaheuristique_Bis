package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;

//SRPT (Shortest Remaining Processing Time)
// donne la priorité à la tâche appartenant au job ayant la plus petite durée restante

public class SrptSolver implements Solver {
    @Override
    public Result solve(Instance instance, long deadline) {
        Task init[] = new Task[instance.numJobs]; // Tableau pour Initialisation
        ArrayList<Task> realisables = new ArrayList<Task>();
        int[] tachesCourant = new int[instance.numJobs]; // actualisation du numéro de la dernière tache effectuée selon le job
        int nbTachesTotal = instance.numJobs*instance.numTasks;
        ResourceOrder resource = new ResourceOrder(instance);

        // Initialisation
        Initialisation(instance,realisables);

        //Boucle: tant qu’il y a des tâches réalisables
        while(nbTachesTotal >0){

            //placer cette tâche sur la ressource qu’elle demande
            Task aux = Srpt(instance,realisables, tachesCourant);
            resource.tasksByMachine[instance.machine(aux.job,aux.task)][resource.nextFreeSlot[instance.machine(aux.job,aux.task)]] = aux;
            resource.nextFreeSlot[instance.machine(aux.job,aux.task)]++;
            TacheCourante(tachesCourant,aux); // actualisation du tableau de taches courantes

            //verifier si la tache à un suivant
            if(aux.task+1<instance.numTasks){
                //créer nouvelle tache
                realisables.add(new Task(aux.job,(aux.task+1)));
            }
            realisables.remove(aux);
            nbTachesTotal--;
        }
        return new Result(instance, resource.toSchedule(), Result.ExitCause.Blocked);
    }


    public static Task Srpt(Instance inst, ArrayList<Task> real, int[] tacheC){
        //prio au job ayant la duréée restante la plus courte
        int[] dureeRestante = new int[inst.numJobs];
        int aux, indiceJob;
        Task auxT = real.get(0);

        for(int i=0; i<inst.numJobs ; i++){
            dureeRestante[i] = 0;
            if((tacheC[i])<inst.numTasks){
                for(int j = (tacheC[i]+1); j< inst.numTasks; j++){
                    dureeRestante[i]=dureeRestante[i]+ inst.duration(i,j);
                }
            }
        }

        aux = dureeRestante[0];
        indiceJob = 0;
        for(int i=0; i<dureeRestante.length; i++){
            if((dureeRestante[i] < aux)){
                aux = dureeRestante[i];
                indiceJob = i;
            }
        }

        for(int j=0; j<real.size(); j++){
            if(real.get(j).job == indiceJob && (real.get(j).task<inst.numTasks)){
                auxT = real.get(j);
            }
        }
        return auxT;
    }

    public static void Initialisation(Instance instance, ArrayList<Task> realisables) {
        for (int j = 0; j < instance.numJobs; j++) {
            realisables.add(new Task(j,0));
        }
    }

    public static void TacheCourante(int[] tabTCourantes, Task ajout){
        for(int i =0; i<tabTCourantes.length; i++){
            if(ajout.job == i){
                tabTCourantes[i]=ajout.task;
            }
        }
    }

}
