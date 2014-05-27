public class Chopstick {
    Thread holder = null;
		
    public synchronized void grab() throws InterruptedException {
			while (holder != null)
				wait();
			holder = Thread.currentThread();
    }

    public synchronized void release() {
			holder = null;
			notify();
    }

    public synchronized void releaseIfMine() {
			if (holder == Thread.currentThread())
				holder = null;
			notify();
    }
}
