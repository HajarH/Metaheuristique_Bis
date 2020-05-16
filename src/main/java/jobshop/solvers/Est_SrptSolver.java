package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;

public class Est_SrptSolver implements Solver {
    @Override
    public Result solve(Instance instance, long deadline) {
        Task init[] = new Task[instance.numJobs]; // Tableau pour Initialisation
        int duree[] = new int[instance.numJobs]; //on calcule la somme des temps des taches executés pour chaque job
        int machines[] = new int[instance.numMachines];
        Task aux;
        ArrayList<Task> realisables = new ArrayList<Task>();
        int[] tachesCourant = new int[instance.numJobs]; // actualisation du numéro de la dernière tache effectuée selon le job
        int nbTachesTotal = instance.numJobs*instance.numTasks;
        ResourceOrder resource = new ResourceOrder(instance);

        // Initialisation
        Initialisation(instance,realisables);

        //Boucle: tant qu’il y a des tâches réalisables
        while(nbTachesTotal >0){

            //placer cette tâche sur la ressource qu’elle demande
            aux = Srpt(instance,realisables,tachesCourant,duree,machines);
            resource.tasksByMachine[instance.machine(aux.job,aux.task)][resource.nextFreeSlot[instance.machine(aux.job,aux.task)]] = aux;
            resource.nextFreeSlot[instance.machine(aux.job,aux.task)]++;
            TacheCourante(tachesCourant,aux); // actualisation du tableau de taches courantes

            //verifier si la tache à un suivant
            if(aux.task+1<instance.numTasks){
                //créer nouvelle tache
                realisables.add(new Task(aux.job,(aux.task+1)));
            }
            realisables.remove(aux);
            machines[instance.machine(aux)] =  Math.max(machines[instance.machine(aux)], duree[aux.job])+instance.duration(aux);
            duree[aux.job] = machines[instance.machine(aux)];
            nbTachesTotal--;
        }
        return new Result(instance, resource.toSchedule(), Result.ExitCause.Blocked);
    }


    public static Task Srpt(Instance instance, ArrayList<Task> taches, int[] tacheC, int[] duree, int[] machine){
        //prio au job ayant la duréée restante la plus courte
        int[] dureeRestante = new int[instance.numJobs];
        int aux, indiceJob;
        Task auxT;

        for(int i=0; i<instance.numJobs ; i++){
            dureeRestante[i] = 0;
            if((tacheC[i])<instance.numTasks){
                for(int j = (tacheC[i]+1); j< instance.numTasks; j++){
                    dureeRestante[i]=dureeRestante[i]+ instance.duration(i,j);
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
        auxT = taches.get(0);
        for(int j=1; j< taches.size();j++){
            if(Math.max(duree[taches.get(j).job],machine[instance.machine(taches.get(j))] ) < Math.max(duree[auxT.job],machine[instance.machine(auxT)])){
                auxT = taches.get(j);
            }
            else if(Math.max(duree[taches.get(j).job],machine[instance.machine(taches.get(j))] ) == Math.max(duree[auxT.job],machine[instance.machine(auxT)])){
                if(taches.get(j).job == indiceJob && (taches.get(j).task<instance.numTasks)){
                    auxT = taches.get(j);
                }
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
