import java.util.concurrent.Semaphore;
import java.util.concurrent.CyclicBarrier;

public class MergeSortWithThreads {
    private static Semaphore semaphore = new Semaphore(1);
    private static CyclicBarrier barrier;
    
    public static void main(String[] args) {
        int[] arr = {12, 11, 13, 5, 6, 7};
        int n = arr.length;

        try {
            barrier = new CyclicBarrier(n);
            Thread t = new Thread(new MergeSortTask(arr, 0, n - 1));
            t.start();
            t.join();

            System.out.println("Arreglo ordenado:");
            for (int i : arr) {
                System.out.print(i + " ");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class MergeSortTask implements Runnable {
        private int[] arr;
        private int left;
        private int right;

        public MergeSortTask(int[] arr, int left, int right) {
            this.arr = arr;
            this.left = left;
            this.right = right;
        }

        @Override
        public void run() {
            if (left < right) {
                try {
                    semaphore.acquire();
                    int mid = (left + right) / 2;
                    MergeSortTask leftTask = new MergeSortTask(arr, left, mid);
                    MergeSortTask rightTask = new MergeSortTask(arr, mid + 1, right);

                    Thread leftThread = new Thread(leftTask);
                    Thread rightThread = new Thread(rightTask);

                    leftThread.start();
                    rightThread.start();

                    leftThread.join();
                    rightThread.join();

                    merge(left, mid, right);

                    semaphore.release();
                    
                    barrier.await(); // Espera a que todos los subprocesos terminen de ordenar
                } catch (InterruptedException | java.util.concurrent.BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }

        private void merge(int left, int mid, int right) {
            int n1 = mid - left + 1;
            int n2 = right - mid;

            int[] leftArr = new int[n1];
            int[] rightArr = new int[n2];

            for (int i = 0; i < n1; ++i)
                leftArr[i] = arr[left + i];
            for (int j = 0; j < n2; ++j)
                rightArr[j] = arr[mid + 1 + j];

            int i = 0, j = 0;

            int k = left;
            while (i < n1 && j < n2) {
                if (leftArr[i] <= rightArr[j]) {
                    arr[k] = leftArr[i];
                    i++;
                } else {
                    arr[k] = rightArr[j];
                    j++;
                }
                k++;
            }

            while (i < n1) {
                arr[k] = leftArr[i];
                i++;
                k++;
            }

            while (j < n2) {
                arr[k] = rightArr[j];
                j++;
                k++;
            }
        }
    }
}
