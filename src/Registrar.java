import tester.Tester;

class Course {
  String name;
  Instructor prof;
  IList<Student> students;

  Course(String name, Instructor prof) { // Courses must have an instructor.
    prof.courses = new ConsList<Course>(this, prof.courses);
    this.name = name;
    this.prof = prof;
    this.students = new MtList<Student>();
  }

  public boolean inThisCourse(Student target) {
    return new OneOfStudents(this.students, target).apply(target);
  }
}

class Instructor {
  String name;
  IList<Course> courses;

  Instructor(String name) {
    this.name = name;
    this.courses = new MtList<Course>();
  }
}

class Student {
  String name;
  int id;
  IList<Course> courses;

  Student(String name, int id) {
    this.name = name;
    this.id = id;
    this.courses = new MtList<Course>();
  }

  // Enrolls this student in the list of students for the given course.
  void enroll(Course c) {
    c.students = new ConsList<Student>(this, c.students);
    this.courses = new ConsList<Course>(c, this.courses); 
    // ^^  Recently added this. When enrolled, the course should also appear in this student's list of courses, right?
  }

  // Determines if the given student is the same as this one
  boolean sameStudent(Student target) {
    return this.name.equals(target.name) && this.id == target.id;
  }

  // Determines if the given student (target) is in any of the same courses as
  // THIS one
  boolean classmates(Student given) {
    return new InCourses(this.courses).apply(given); // complete
  }
}

interface IFunc<A, R> {
  R apply(A arg);
}

interface IListVisitor<T, R> extends IFunc<T, R> {
  R forMt(MtList<T> arg);

  R forCons(ConsList<T> arg);
}

// Because a predicate is a specialized variety of function that is expected to return a boolean
interface IPred<X> extends IFunc<X, Boolean> {
}

class InCourses implements IPred<Student> {
  IList<Course> coursePool;

  InCourses(IList<Course> coursePool) {
    this.coursePool = coursePool;
  }

  public Boolean apply(Student arg) {
    return this.coursePool.accept(new InRoster(arg));
  }
}

class InRoster implements IListVisitor<Course, Boolean> {
  Student target;

  InRoster(Student target) {
    this.target = target;
  }

  public Boolean apply(Course arg) {
    return null; // Have not used this... what to do here?
  }

  public Boolean forMt(MtList<Course> arg) {
    return false;
  }

  public Boolean forCons(ConsList<Course> arg) {
    return arg.first.inThisCourse(this.target) || arg.rest.accept(new InRoster(this.target));
  }
}

class OneOfStudents implements IListVisitor<Student, Boolean> {
  IList<Student> studentPool;
  Student target;

  OneOfStudents(IList<Student> studentPool, Student target) {
    this.studentPool = studentPool;
    this.target = target;
  }

  public Boolean apply(Student arg) {
    return this.studentPool.accept(this);
  }

  public Boolean forMt(MtList<Student> arg) {
    return false;
  }

  public Boolean forCons(ConsList<Student> arg) {
    return arg.first.sameStudent(target) || new OneOfStudents(arg.rest, target).apply(target);
  }

}

interface IList<T> {
  // Accepts a visitor and determines if the IList is Mt or Cons
  <R> R accept(IListVisitor<T, R> visitor);
}

class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  public <R> R accept(IListVisitor<T, R> visitor) {
    return visitor.forCons(this);
  }
}

class MtList<T> implements IList<T> {

  public <R> R accept(IListVisitor<T, R> visitor) {
    return visitor.forMt(this);
  }
}

class ExamplesCourses {
  Instructor AMislove = new Instructor("Alan Mislove");
  Instructor JHemann = new Instructor("Jason Hemann");
  Instructor BLerner = new Instructor("Ben Lerner");
  Instructor Moses = new Instructor("Moses");
  Instructor CyberProf = new Instructor("CyberProf");
  Course CS2500 = new Course("Fundamentals of Computer Science I", this.AMislove);
  Course CS2510 = new Course("Fundamentals of Computer Science II", this.JHemann);
  Course CS3500 = new Course("Object Oriented Design", this.BLerner);
  Course Linear = new Course("Linear Mathematics", this.Moses);
  Course Cyber = new Course("Cyber", this.CyberProf);

  Student DWang = new Student("Daniel Wang", 1);
  Student SR = new Student("Sauharda Rajbhandari", 2);
  Student Preston = new Student("Preston R", 3);

  void testing(Tester t) {
    Student DWang = new Student("Daniel Wang", 1);
    t.checkExpect(DWang.courses, new MtList<Course>());
    DWang.enroll(this.CS2510);
    t.checkExpect(DWang.courses, new ConsList<Course>(this.CS2510, new MtList<Course>()));
  }
  
  void testingClassmatesMethod(Tester t) {
    Student DWang = new Student("Daniel Wang", 1);
    Student SR = new Student("Sauharda Rajbhandari", 2);
    Student Preston = new Student("Preston R", 3);
    t.checkExpect(SR.classmates(DWang), false);
    DWang.enroll(CS3500);
    SR.enroll(CS3500);
    Preston.enroll(Cyber);
    t.checkExpect(DWang.classmates(Preston), false);
    t.checkExpect(Preston.classmates(DWang), false);
    t.checkExpect(SR.classmates(Preston), false);
    t.checkExpect(Preston.classmates(SR), false);
    t.checkExpect(DWang.classmates(SR), true);
    t.checkExpect(SR.classmates(DWang), true);
    SR.enroll(Cyber);
    t.checkExpect(SR.classmates(Preston), true);
    t.checkExpect(DWang.classmates(Preston), false);
  }

}