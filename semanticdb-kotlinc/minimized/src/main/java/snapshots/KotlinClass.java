package snapshots;

public class KotlinClass {
    KotlinClass() throws Class {
       throw new Class();
    }

    void test() throws Class {
        Class c = new Class(1, "");
        System.out.println(c.getBanana());
        c.setBanana(5);
        throw c;
    }

    void other() throws Class {
       throw new Class(1);
    }
}
