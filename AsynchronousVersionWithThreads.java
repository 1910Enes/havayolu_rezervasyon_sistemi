import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Flight {
    String flightId;
    boolean[] seats;

    public Flight(String flightId, int numSeats) {
        this.flightId = flightId;
        this.seats = new boolean[numSeats];
    }

    public boolean querySeat(int seatNumber) {
        return seats[seatNumber];
    }

    public boolean makeReservation(int seatNumber) {
        if (!seats[seatNumber]) {
            simulateDelay(); // Simüle edilmiş gecikme
            seats[seatNumber] = true;
            return true;
        }
        return false;
    }

    public boolean cancelReservation(int seatNumber) {
        if (seats[seatNumber]) {
            seats[seatNumber] = false;
            return true;
        }
        return false;
    }

    // Simüle edilmiş bir gecikme ekleyelim
    private void simulateDelay() {
        try {
            Thread.sleep(new Random().nextInt(1000)); // Rastgele 0-1000 milisaniye arası bir gecikme
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Database {
    List<Flight> flights = new ArrayList<>();

    public void addFlight(Flight flight) {
        flights.add(flight);
    }

    public Flight getFlight(String flightId) {
        for (Flight flight : flights) {
            if (flight.flightId.equals(flightId)) {
                return flight;
            }
        }
        return null;
    }
}

public class AsynchronousVersionWithThreads {
    public static void main(String[] args) {
        System.out.println("Asenkron Versiyon (Thread'ler) çalıştırılıyor...");
        Database db = new Database();
        Flight flight1 = new Flight("Flight2", 5); // 5 koltuk
        db.addFlight(flight1);

        ExecutorService executor = Executors.newFixedThreadPool(6); // 6 thread'li bir executor oluşturduk

        // Yazıcı görevleri submit et
        for (int i = 1; i <= 6; i++) {
            String threadName = "Yazıcı-" + i;
            int seatNumber = 1; // Hepsi aynı koltuğa yazacaklar
            executor.submit(new SeatWriter(db, "Flight2", seatNumber, threadName));
        }

        // ExecutorService'i kapat
        executor.shutdown();
    }
}

class SeatWriter implements Runnable {
    private Database db;
    private String flightId;
    private int seatNumber;
    private String threadName;

    public SeatWriter(Database db, String flightId, int seatNumber, String threadName) {
        this.db = db;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.threadName = threadName;
    }

    @Override
    public void run() {
        Flight flight = db.getFlight(flightId);
        if (flight != null) {
            if (flight.makeReservation(seatNumber)) {
                System.out.println(threadName + " yazma işlemiyle koltuk " + seatNumber + "'yi rezerve etmeye çalışıyor.");
                System.out.println(threadName + " koltuk " + seatNumber + "'yi başarıyla rezerve etti.");
            } else {
                System.out.println(threadName + " yazma işlemiyle koltuk " + seatNumber + "'yi rezerve etmeye çalışıyor.");
                System.out.println(threadName + " koltuk " + seatNumber + "'yi zaten dolu olduğu için rezerve edemedi.");
            }
        }
    }
}
