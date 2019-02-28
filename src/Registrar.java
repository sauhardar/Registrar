import tester.Tester;

// Represents a single course
class Course {
  String name;
  Instructor prof;
  IList<Student> students;

  Course(String name, Instructor prof) { // Courses must have an instructor.
    this.name = name;
    this.prof = prof;
    this.students = new MtList<Student>();
    prof.courses = new ConsList<Course>(this, prof.courses);
  }

  // Determines if the given student is in the list of students of this course.
  public boolean inThisCourse(Student target) {
    return true;
  }
}
// Represents an instructor

class Instructor {
  String name;
  IList<Course> courses;

  // An Instructor starts with a no courses (empty list)
  Instructor(String name) {
    this.name = name;
    this.courses = new MtList<Course>();
  }

  // Is the given Student is in more than one of this Instructor’s Courses?
  boolean dejavu(Student target) {
    int coursesTaken = new MultipleCourses(this.courses, target).apply(null);

    return coursesTaken > 1;
    // Determines if the number of courses taken with THIS instructor
    // is greater than 1.
  }
}

// Represents a student
class Student {
  String name;
  int id;
  IList<Course> courses;

  // Initially, a student is taking no courses until enrolled in one or more.
  Student(String name, int id) {
    this.name = name;
    this.id = id;
    this.courses = new MtList<Course>();
  }

  // Enrolls this student in the list of students for the given course. Updates
  // the course's roster to include THIS student, and updates THIS student's
  // list of courses to include the given course.
  void enroll(Course c) {
    c.students = new ConsList<Student>(this, c.students);
    this.courses = new ConsList<Course>(c, this.courses);
  }

  // Determines if the given student is the same as this one by checking name id.
  boolean sameStudent(Student target) {
    return this.name.equals(target.name) && this.id == target.id;
  }

  // Determines if the given student is in any of the same courses as THIS one
  boolean classmates(Student target) {
    return new InCourses(this.courses).apply(target);
  }
}

// An interface that represents a function object
interface IFunc<A, R> {
  // Applies a function that goes from A->R
  R apply(A arg);
}

// An IListVisitor
interface IListVisitorIList<T, R> extends IFunc<IList<T>, R> {

  // Generic function for an MtList
  R forMt(MtList<T> arg);

  // Generic function for a ConsList
  R forCons(ConsList<T> arg);
}

// An interface representing a general list.
interface IList<T> {
  // Accepts a visitor and determines if the IList is Mt or Cons
  <R> R accept(IListVisitorIList<T, R> visitor);
}

// A class representing a non-empty list of courses
class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  // Applies the given visitor's forCons method on this non-empty list.
  public <R> R accept(IListVisitorIList<T, R> visitor) {
    return visitor.forCons(this);
  }

}

// A class representing an empt list of courses
class MtList<T> implements IList<T> {
  // Applies the given visitor's forCons method on this empty list.
  public <R> R accept(IListVisitorIList<T, R> visitor) {
    return visitor.forMt(this);
  }

}

class ExamplesCourses {
  Instructor AMislove;
  Instructor JHemann;
  Instructor BLerner;
  Instructor Moses;
  Instructor CyberProf;
  Course CS2500;
  Course CS2510;
  Course CS3500;
  Course Linear;
  Course Cyber;
  Course CS3700;
  Course Cyber2;

  Student DWang;
  Student SR;
  Student Preston;
  Student Ethan;
  Student LiAnn;

  void reset() { // Resets all values to their original values
    AMislove = new Instructor("Alan Mislove");
    JHemann = new Instructor("Jason Hemann");
    BLerner = new Instructor("Ben Lerner");
    Moses = new Instructor("Moses");
    CyberProf = new Instructor("CyberProf");
    CS2500 = new Course("Fundamentals of Computer Science I", this.AMislove);
    CS2510 = new Course("Fundamentals of Computer Science II", this.JHemann);
    CS3500 = new Course("Object Oriented Design", this.BLerner);
    Linear = new Course("Linear Mathematics", this.Moses);
    Cyber = new Course("Cyber", this.CyberProf);
    CS3700 = new Course("Algo", this.Moses);
    Cyber2 = new Course("Cyber 2", this.CyberProf);

    DWang = new Student("Daniel Wang", 1);
    SR = new Student("Sauharda Rajbhandari", 2);
    Preston = new Student("Preston R", 3);
    Ethan = new Student("Ethan P", 4);
    LiAnn = new Student("LiAnn B", 5);

  }

  IList<Course> allCourses = new ConsList<Course>(this.CS2500,
      new ConsList<Course>(this.CS2510,
          new ConsList<Course>(this.Linear,
              new ConsList<Course>(this.Cyber, new ConsList<Course>(this.CS3700,
                  new ConsList<Course>(this.CS3500, new MtList<Course>()))))));

  void testEnroll(Tester t) {
    reset();
    t.checkExpect(this.DWang.courses, new MtList<Course>());
    this.DWang.enroll(Linear);

    t.checkExpect(this.DWang.courses, new ConsList<Course>(this.Linear, new MtList<Course>()));
    this.DWang.enroll(CS2500);
    t.checkExpect(this.DWang.courses,
        new ConsList<Course>(CS2500, new ConsList<Course>(this.Linear, new MtList<Course>())));
    t.checkExpect(this.Preston.courses, new MtList<Course>());
    this.Preston.enroll(CS2510);
    t.checkExpect(this.Preston.courses, new ConsList<Course>(this.CS2510, new MtList<Course>()));
    this.Preston.enroll(CS2500);
    t.checkExpect(this.Preston.courses,
        new ConsList<Course>(this.CS2500, new ConsList<Course>(this.CS2510, new MtList<Course>())));
  }

  boolean testSameStudent(Tester t) {
    reset();
    return t.checkExpect(this.Preston.sameStudent(this.DWang), false)
        && t.checkExpect(this.Preston.sameStudent(this.SR), false)
        && t.checkExpect(this.Preston.sameStudent(this.LiAnn), false)
        && t.checkExpect(this.Preston.sameStudent(this.Preston), true)
        && t.checkExpect(this.DWang.sameStudent(this.Preston), false)
        && t.checkExpect(this.SR.sameStudent(this.Preston), false)
        && t.checkExpect(this.LiAnn.sameStudent(this.Preston), false)
        && t.checkExpect(this.LiAnn.sameStudent(this.LiAnn), true);
  }

  void testClassmates(Tester t) {
    reset();
    t.checkExpect(this.Preston.classmates(this.Ethan), false);
    this.Preston.enroll(this.Cyber);
    this.Ethan.enroll(this.Cyber);
    t.checkExpect(this.Preston.classmates(this.Ethan), true);
    t.checkExpect(this.Ethan.classmates(this.LiAnn), false);
    this.LiAnn.enroll(this.CS2500);
    this.DWang.enroll(this.CS2500);
    this.SR.enroll(this.CS2500);
    t.checkExpect(this.LiAnn.classmates(this.DWang), true);
    t.checkExpect(this.DWang.classmates(this.SR), true);
    t.checkExpect(this.SR.classmates(this.LiAnn), true);
    t.checkExpect(this.LiAnn.classmates(this.SR), true);
    t.checkExpect(this.SR.classmates(this.DWang), true);
    t.checkExpect(this.DWang.classmates(this.LiAnn), true);

    t.checkExpect(this.DWang.classmates(this.Ethan), false);
    this.Ethan.enroll(this.Linear);
    this.DWang.enroll(this.Linear);
    t.checkExpect(this.DWang.classmates(this.Ethan), true);
    t.checkExpect(this.SR.classmates(this.Ethan), false);
    t.checkExpect(this.LiAnn.classmates(this.Ethan), false);
  }

  void testInThisCourse(Tester t) {
    reset();

    t.checkExpect(this.CS2500.inThisCourse(this.DWang), false);
    t.checkExpect(this.CS2510.inThisCourse(this.SR), false);
    t.checkExpect(this.CS3500.inThisCourse(this.Preston), false);
    t.checkExpect(this.CS3700.inThisCourse(this.LiAnn), false);

    this.DWang.enroll(this.CS2500);
    this.SR.enroll(this.CS2510);
    this.Preston.enroll(this.CS3500);
    this.LiAnn.enroll(this.CS3700);

    t.checkExpect(this.CS2500.inThisCourse(this.DWang), true);
    t.checkExpect(this.CS2510.inThisCourse(this.SR), true);
    t.checkExpect(this.CS3500.inThisCourse(this.Preston), true);
    t.checkExpect(this.CS3700.inThisCourse(this.LiAnn), true);
    t.checkExpect(this.Linear.inThisCourse(this.SR), false);
    this.SR.enroll(this.Linear);
    t.checkExpect(this.Linear.inThisCourse(this.SR), true);
  }

  void testDejavu(Tester t) {
    reset();
    this.DWang.enroll(this.Linear); // taught by moses.
    this.DWang.enroll(this.CS3700); // taught by moses.
    t.checkExpect(this.Moses.dejavu(this.DWang), true);
    t.checkExpect(this.Moses.dejavu(this.SR), false);
    t.checkExpect(this.Moses.dejavu(this.Preston), false);
    t.checkExpect(this.AMislove.dejavu(this.DWang), false);
    t.checkExpect(this.AMislove.dejavu(this.SR), false);
    t.checkExpect(this.AMislove.dejavu(this.Preston), false);

    this.Preston.enroll(this.Cyber);
    t.checkExpect(this.CyberProf.dejavu(this.Preston), false);

    this.Preston.enroll(this.Cyber2);
    t.checkExpect(this.CyberProf.dejavu(this.Preston), true);
  }

  void testApply(Tester t) {
    reset();
    this.DWang.enroll(this.Linear); // taught by moses.
    this.DWang.enroll(this.CS3700); // taught by moses.
  }

  void testForMt(Tester t) {
    reset();
  }

  void testForCons(Tester t) {
    reset();
    this.DWang.enroll(this.Linear); // taught by moses.
    this.DWang.enroll(this.CS3700); // taught by moses.
  }
}
