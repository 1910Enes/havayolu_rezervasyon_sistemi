import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Uçuş {
    String uçuşId;
    boolean[] koltuklar;
    private final ReentrantReadWriteLock kilit = new ReentrantReadWriteLock(true);

    public Uçuş(String uçuşId, int koltukSayısı) {
        this.uçuşId = uçuşId;
        this.koltuklar = new boolean[koltukSayısı];
    }

    public boolean koltukDurumu(int koltukNumarası) {
        kilit.readLock().lock();
        try {
            return koltuklar[koltukNumarası];
        } finally {
            kilit.readLock().unlock();
        }
    }

    public boolean rezervasyonYap(int koltukNumarası) {
        kilit.writeLock().lock();
        try {
            if (!koltuklar[koltukNumarası]) {
                koltuklar[koltukNumarası] = true;
                return true;
            }
            return false;
        } finally {
            kilit.writeLock().unlock();
        }
    }

    public boolean rezervasyonIptal(int koltukNumarası) {
        kilit.writeLock().lock();
        try {
            if (koltuklar[koltukNumarası]) {
                koltuklar[koltukNumarası] = false;
                return true;
            }
            return false;
        } finally {
            kilit.writeLock().unlock();
        }
    }
}

class Veritabanı {
    private final List<Uçuş> uçuşlar = new ArrayList<>();

    public void uçuşEkle(Uçuş uçuş) {
        uçuşlar.add(uçuş);
    }

    public Uçuş uçuşBul(String uçuşId) {
        for (Uçuş uçuş : uçuşlar) {
            if (uçuş.uçuşId.equals(uçuşId)) {
                return uçuş;
            }
        }
        return null;
    }
}

class Reader extends Thread {
    private final Veritabanı vt;
    private final String uçuşId;

    public Reader(Veritabanı vt, String uçuşId, String isim) {
        super(isim);
        this.vt = vt;
        this.uçuşId = uçuşId;
    }

    public void run() {
        Uçuş uçuş = vt.uçuşBul(uçuşId);
        if (uçuş != null) {
            StringBuilder koltukDurumları = new StringBuilder();
            for (int i = 0; i < uçuş.koltuklar.length; i++) {
                koltukDurumları.append("Koltuk No ").append(i).append(" : ").append(uçuş.koltukDurumu(i) ? "1" : "0").append(" ");
            }
            System.out.println(Thread.currentThread().getName() + " mevcut koltukları kontrol ediyor. Koltukların durumu: ");
            System.out.println(koltukDurumları.toString());
        }
    }
}

class Writer extends Thread {
    private final Veritabanı vt;
    private final String uçuşId;
    private final int koltukNumarası;

    public Writer(Veritabanı vt, String uçuşId, int koltukNumarası, String isim) {
        super(isim);
        this.vt = vt;
        this.uçuşId = uçuşId;
        this.koltukNumarası = koltukNumarası;
    }

    public void run() {
        Uçuş uçuş = vt.uçuşBul(uçuşId);
        if (uçuş != null) {
            if (uçuş.rezervasyonYap(koltukNumarası)) {
                System.out.println(Thread.currentThread().getName() + " " + koltukNumarası + " numaralı koltuğu rezerve etmeye çalışıyor.");
                System.out.println(Thread.currentThread().getName() + " " + koltukNumarası + " numaralı koltuğu başarıyla rezerve etti.");
            } else {
                System.out.println(Thread.currentThread().getName() + " " + koltukNumarası + " numaralı koltuğu rezerve etmeye çalışıyor.");
                System.out.println(Thread.currentThread().getName() + " " + koltukNumarası + " numaralı koltuğu rezerve edemedi, çünkü zaten rezerve edilmiş.");
            }
        }
    }
}

public class SynchronousVersion {
    public static void main(String[] args) {
        Veritabanı vt = new Veritabanı();
        Uçuş uçuş1 = new Uçuş("Uçuş1", 5); // 5 koltuk
        vt.uçuşEkle(uçuş1);

        // Önce Reader thread'lerini başlatıyoruz
        new Reader(vt, "Uçuş1", "reader-1").start();
        new Reader(vt, "Uçuş1", "reader-2").start();
        new Reader(vt, "Uçuş1", "reader-3").start();

        try {
            Thread.sleep(100); // Okuyucuların önceliği için kısa bir bekleme
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Şimdi de Writer thread'lerini başlatıyoruz
        new Writer(vt, "Uçuş1", 1, "writer-1").start();
        new Writer(vt, "Uçuş1", 1, "writer-2").start();
        new Writer(vt, "Uçuş1", 1, "writer-3").start();
    }
}
