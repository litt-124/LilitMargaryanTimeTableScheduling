public class Autoassociator {
	private int weights[][];
	private int trainingCapacity;
	
	public Autoassociator(CourseArray courses) {
		int numCourses = courses.length(); 
        weights = new int[numCourses][numCourses];
        trainingCapacity = 0;
		}
	
	public int getTrainingCapacity() {
        return trainingCapacity;
	}
	
	public void training(int clashes[]) {
		int n = clashes.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    weights[i][j] -= clashes[i] * clashes[j];
                }
            }
        }
        trainingCapacity++;
		}
        public int predictBestSlot() {
            int[] clashTotals = new int[weights.length];
            for (int i = 0; i < weights.length; i++) {
                for (int j = 0; j < weights[i].length; j++) {
                    clashTotals[i] += weights[i][j];
                }
            }
    
            int minIndex = 0;
            for (int i = 1; i < clashTotals.length; i++) {
                if (clashTotals[i] > clashTotals[minIndex]) {
                    minIndex = i;
                }
            }
    
            return minIndex;
        }
	public int unitUpdate(int neurons[]) {
		int index = (int) (Math.random() * neurons.length);
        unitUpdate(neurons, index);
        return index;
	}
	
	public void unitUpdate(int neurons[], int index) {
		int sum = 0;
        for (int i = 0; i < neurons.length; i++) {
            if (i != index) {
                sum += weights[index][i] * neurons[i];
            }
        }
        neurons[index] = sum > 0 ? 1 : -1;
		}
	
	public void chainUpdate(int neurons[], int steps) {
		for (int i = 0; i < steps; i++) {
            unitUpdate(neurons);
        }	}
	
	public void fullUpdate(int neurons[]) {
		boolean changed;
        do {
            changed = false;
            int[] previousState = neurons.clone();
            for (int i = 0; i < neurons.length; i++) {
                unitUpdate(neurons, i);
            }
            if (!java.util.Arrays.equals(previousState, neurons)) {
                changed = true;
            }
        } while (changed);
		}
}
