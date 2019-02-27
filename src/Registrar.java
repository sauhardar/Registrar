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

  // Determines if the given student is in the list of students of this course.
  public boolean inThisCourse(Student target) {
    return new OneOfStudents(this.students, target).apply(target);
    // OneOfStudents takes in two things. Seems like function objects usually only
    // take in one... is this okay? !!!
  }
}
// Represents an instructor

class Instructor {
  String name;
  IList<Course> courses;

  // An Instructor starts with a list of no courses—initially not
  // teaching any courses until assigned to them.
  Instructor(String name) {
    this.name = name;
    this.courses = new MtList<Course>();
  }

  // determines whether the given Student is in more than one of this Instructor’s
  // Courses.
  boolean dejavu(Student target) {
    int coursesTaken = new MultipleCourses(this.courses, target).apply(null);
    // null here... it doesn't actually use the Course that it's given.
    return coursesTaken > 1;
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
    // ^^ Recently added this. When enrolled, the course should also appear in this
    // student's list of courses, right?
  }

  // Determines if the given student is the same as
  // this one by checking name id.
  boolean sameStudent(Student target) {
    return this.name.equals(target.name) && this.id == target.id;
  }

  // Determines if the given student (target) is in any of the same courses as
  // THIS one
  boolean classmates(Student target) {
    return new InCourses(this.courses).apply(target);
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

// A function object that determines if a student is a part of a list of courses.
class InCourses implements IPred<Student> {
  IList<Course> coursePool;

  InCourses(IList<Course> coursePool) {
    this.coursePool = coursePool;
  }

  // Dispatches to forMt or forCons with the function object InRoster depending on
  // whether the main student's list of courses is empty or not empty.
  public Boolean apply(Student arg) {
    return this.coursePool.accept(new InRoster(arg));
  }
}

// The function object that determines whether a given student is a part of a list of students.
class InRoster implements IListVisitor<Course, Boolean> {
  Student target;

  InRoster(Student target) {
    this.target = target;
  }

  // Not being used... !!!
  public Boolean apply(Course arg) {
    return null; // Have not used this... what to do here?
  }

  // If a list of courses is empty, a student is not in the list of course.
  public Boolean forMt(MtList<Course> arg) {
    return false;
  }

  // For a non-empty list of courses, determines if the given student is in the
  // roster for the first course or in the rosters of the remaining courses.
  public Boolean forCons(ConsList<Course> arg) {
    return arg.first.inThisCourse(this.target) || arg.rest.accept(new InRoster(this.target));
  }
}

// A function object that determines if a given student is in the given pool of students.
class OneOfStudents implements IListVisitor<Student, Boolean> {
  IList<Student> studentPool;
  Student target;

  OneOfStudents(IList<Student> studentPool, Student target) {
    this.studentPool = studentPool;
    this.target = target;
  }

  // Dispatches to forMt or forCons depending on whether the studentPool is empty
  public Boolean apply(Student arg) {
    return this.studentPool.accept(this);
    // the student that is given to this method is not being used... !!!
  }

  // If the student pool is empty, obviously the given
  // student is not a part of the list (it's empty!).
  public Boolean forMt(MtList<Student> arg) {
    return false;
  }

  // Determines if the given student is the first
  // student in the student pool (list) OR the rest.
  public Boolean forCons(ConsList<Student> arg) {
    return arg.first.sameStudent(target) || new OneOfStudents(arg.rest, target).apply(target);
  }
}

class MultipleCourses implements IListVisitor<Course, Integer> {
  IList<Course> profsCourses;
  Student target;

  MultipleCourses(IList<Course> profsCourses, Student target) {
    this.profsCourses = profsCourses;
    this.target = target;
  }

  // are these supposed to return int?
  public Integer apply(Course arg) {
    return this.profsCourses.accept(this);
  }

  public Integer forMt(MtList<Course> arg) {
    return 0;
  }

  public Integer forCons(ConsList<Course> arg) {
    if (arg.first.inThisCourse(this.target)) {
      // accessing so many fields here... this is so illegal
      return 1 + new MultipleCourses(arg.rest, this.target).apply(arg.first);
      // this arg.first is just here because the apply NEEDS a
      // course...otherwise it's never used.
    }
    else {
      return new MultipleCourses(arg.rest, this.target).apply(arg.first);
    }
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

// make sure to check constructors and similar stuff if necessary
class ExamplesCourses {
  // We refrained from using "this.(...)" in our void methods because
  // doing so would mutate the overall/global examples at the top of this
  // examples class. This way, we only refer to the examples within each
  // method.

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
  Course CS3700 = new Course("Algo", this.Moses);
  Course Cyber2 = new Course("Cyber 2", this.CyberProf);

  Student DWang = new Student("Daniel Wang", 1);
  Student SR = new Student("Sauharda Rajbhandari", 2);
  Student Preston = new Student("Preston R", 3);
  Student Ethan = new Student("Ethan P", 4);
  Student LiAnn = new Student("LiAnn B", 5);

  IList<Course> allCourses = new ConsList<Course>(this.CS2500,
      new ConsList<Course>(this.CS2510,
          new ConsList<Course>(this.Linear,
              new ConsList<Course>(this.Cyber, new ConsList<Course>(this.CS3700,
                  new ConsList<Course>(this.CS3500, new MtList<Course>()))))));

  void testingEnroll(Tester t) {
    Student DWang = new Student("Daniel Wang", 1);
    Student Preston = new Student("Preston R", 3);

    Course CS2500 = new Course("Fundamentals of Computer Science I", AMislove);
    Course CS2510 = new Course("Fundamentals of Computer Science II", JHemann);
    Course Linear = new Course("Linear Mathematics", Moses);

    t.checkExpect(DWang.courses, new MtList<Course>());
    DWang.enroll(Linear);
    t.checkExpect(DWang.courses, new ConsList<Course>(Linear, new MtList<Course>()));
    DWang.enroll(CS2500);
    t.checkExpect(DWang.courses,
        new ConsList<Course>(CS2500, new ConsList<Course>(Linear, new MtList<Course>())));
    t.checkExpect(Preston.courses, new MtList<Course>());
    Preston.enroll(CS2510);
    t.checkExpect(Preston.courses, new ConsList<Course>(CS2510, new MtList<Course>()));
    Preston.enroll(CS2500);
    t.checkExpect(Preston.courses,
        new ConsList<Course>(CS2500, new ConsList<Course>(CS2510, new MtList<Course>())));
  }

  boolean testingSameStudent(Tester t) {
    return t.checkExpect(Preston.sameStudent(DWang), false)
        && t.checkExpect(Preston.sameStudent(SR), false)
        && t.checkExpect(Preston.sameStudent(LiAnn), false)
        && t.checkExpect(Preston.sameStudent(Preston), true)
        && t.checkExpect(DWang.sameStudent(Preston), false)
        && t.checkExpect(SR.sameStudent(Preston), false)
        && t.checkExpect(LiAnn.sameStudent(Preston), false)
        && t.checkExpect(LiAnn.sameStudent(LiAnn), true);
  }

  void testingClassmates(Tester t) {
    // constants used just for this test
    Student DWang = new Student("Daniel Wang", 1);
    Student SR = new Student("Sauharda Rajbhandari", 2);
    Student Preston = new Student("Preston R", 3);
    Student Ethan = new Student("Ethan P", 4);
    Student LiAnn = new Student("LiAnn B", 5);

    Course CS2500 = new Course("Fundamentals of Computer Science I", AMislove);
    Course Linear = new Course("Linear Mathematics", Moses);
    Course Cyber = new Course("Cyber", CyberProf);

    t.checkExpect(Preston.classmates(Ethan), false);
    Preston.enroll(Cyber);
    Ethan.enroll(Cyber);
    t.checkExpect(Preston.classmates(Ethan), true);
    t.checkExpect(Ethan.classmates(LiAnn), false);
    LiAnn.enroll(CS2500);
    DWang.enroll(CS2500);
    SR.enroll(CS2500);
    t.checkExpect(LiAnn.classmates(DWang), true);
    t.checkExpect(DWang.classmates(SR), true);
    t.checkExpect(SR.classmates(LiAnn), true);
    t.checkExpect(LiAnn.classmates(SR), true);
    t.checkExpect(SR.classmates(DWang), true);
    t.checkExpect(DWang.classmates(LiAnn), true);

    t.checkExpect(DWang.classmates(Ethan), false);
    Ethan.enroll(Linear);
    DWang.enroll(Linear);
    t.checkExpect(DWang.classmates(Ethan), true);
    t.checkExpect(SR.classmates(Ethan), false);
    t.checkExpect(LiAnn.classmates(Ethan), false);
  }

  void testingInThisCourse(Tester t) {
    Student DWang = new Student("Daniel Wang", 1);
    Student SR = new Student("Sauharda Rajbhandari", 2);
    Student Preston = new Student("Preston R", 3);
    Student Ethan = new Student("Ethan P", 4);
    Student LiAnn = new Student("LiAnn B", 5);

    Course CS2500 = new Course("Fundamentals of Computer Science I", AMislove);
    Course CS2510 = new Course("Fundamentals of Computer Science II", JHemann);
    Course CS3500 = new Course("Object Oriented Design", BLerner);
    Course Linear = new Course("Linear Mathematics", Moses);
    Course Cyber = new Course("Cyber", CyberProf);
    Course CS3700 = new Course("Algo", Moses);

    t.checkExpect(CS2500.inThisCourse(DWang), false);
    t.checkExpect(CS2510.inThisCourse(SR), false);
    t.checkExpect(CS3500.inThisCourse(Preston), false);
    t.checkExpect(CS3700.inThisCourse(LiAnn), false);

    DWang.enroll(CS2500);
    SR.enroll(CS2510);
    Preston.enroll(CS3500);
    LiAnn.enroll(CS3700);

    t.checkExpect(CS2500.inThisCourse(DWang), true);
    t.checkExpect(CS2510.inThisCourse(SR), true);
    t.checkExpect(CS3500.inThisCourse(Preston), true);
    t.checkExpect(CS3700.inThisCourse(LiAnn), true);
    t.checkExpect(Linear.inThisCourse(SR), false);
    SR.enroll(Linear);
    t.checkExpect(Linear.inThisCourse(SR), true);

  }

  // tests dejavu
  void testingDejavuMethod(Tester t) {
    Instructor AMislove = new Instructor("Alan Mislove");
    Instructor Moses = new Instructor("Moses");
    Instructor CyberProf = new Instructor("CyberProf");
    Course Linear = new Course("Linear Mathematics", Moses);
    Course CS3700 = new Course("Algo", Moses);
    Course Cyber = new Course("Cyber", CyberProf);
    Course Cyber2 = new Course("Cyber 2", CyberProf);

    Student DWang = new Student("Daniel Wang", 1);
    Student SR = new Student("Sauharda Rajbhandari", 2);
    Student Preston = new Student("Preston R", 3);

    DWang.enroll(Linear); // taught by moses.
    DWang.enroll(CS3700); // taught by moses.
    t.checkExpect(Moses.dejavu(DWang), true);
    t.checkExpect(Moses.dejavu(SR), false);
    t.checkExpect(Moses.dejavu(Preston), false);
    t.checkExpect(AMislove.dejavu(DWang), false);
    t.checkExpect(AMislove.dejavu(SR), false);
    t.checkExpect(AMislove.dejavu(Preston), false);
    Preston.enroll(Cyber);
    t.checkExpect(CyberProf.dejavu(Preston), false);
    Preston.enroll(Cyber2);
    t.checkExpect(CyberProf.dejavu(Preston), true);
  }

}