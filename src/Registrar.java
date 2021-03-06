import tester.Tester;

//Represents a single course
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
    return new OneOfStudents(target).apply(this.students);
  }
}
//Represents an instructor

class Instructor {
  String name;
  IList<Course> courses;

  // An Instructor starts with a list of no courses—initially not
  // teaching any courses until assigned to them.
  Instructor(String name) {
    this.name = name;
    this.courses = new MtList<Course>();
  }

  // Is the given Student is in more than one of this Instructor’s Courses?
  boolean dejavu(Student target) {
    int coursesTaken = new MultipleCourses(target).apply(this.courses);
    return coursesTaken > 1;
    // Determines if the number of courses taken with THIS instructor
    // is greater than 1.
  }
}

//Represents a student
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

  // Determines if the given student (target) is in any of the same courses as
  // THIS one
  boolean classmates(Student target) {
    return new InCourses(this.courses).apply(target);
  }
}

//An interface that represents a function object
interface IFunc<A, R> {
  // Applies a function that goes from A->R
  R apply(A arg);
}

//An IListVisitor
interface IListVisitor<T, R> extends IFunc<IList<T>, R> {

  // Generic function for an MtList
  R forMt(MtList<T> arg);

  // Generic function for a ConsList
  R forCons(ConsList<T> arg);
}

//Because a predicate is a specialized variety of function that is expected to return a boolean
interface IPred<X> extends IFunc<X, Boolean> {
}

//A function object that determines if a student is a part of a list of courses.
class InCourses implements IPred<Student> {
  IList<Course> coursePool;

  InCourses(IList<Course> coursePool) {
    this.coursePool = coursePool;
  }

  // Dispatches to forMt or forCons with the function object InRoster depending on
  // whether the main student's list of courses is empty or not empty.
  public Boolean apply(Student arg) {
    return new InRoster(arg).apply(this.coursePool);
  }
}

//The function object that determines whether a given student is a part of a list of students.
class InRoster implements IListVisitor<Course, Boolean> {
  Student target;

  InRoster(Student target) {
    this.target = target;
  }

  // Dispatches to forMt or forCons with the function object InRoster depending on
  // whether the main student's list of courses is empty or not empty.
  public Boolean apply(IList<Course> arg) {
    return arg.accept(this);
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

  Student target;

  OneOfStudents(Student target) {
    this.target = target;
  }

  // Dispatches to forMt or forCons depending on whether the studentPool is empty
  // within this function obejct
  public Boolean apply(IList<Student> arg) {
    return arg.accept(this);
  }

  // If the student pool is empty, obviously the given
  // student is not a part of the list (it's empty!).
  public Boolean forMt(MtList<Student> arg) {
    return false;
  }

  // Determines if the given student is the first
  // student in the student pool (list) OR the rest.
  public Boolean forCons(ConsList<Student> arg) {
    return arg.first.sameStudent(target) || new OneOfStudents(target).apply(arg.rest);
  }
}

// A function object that determines if a given Student is in a given list of
// Courses, which represents all the courses one particular Instructor teaches.
class MultipleCourses implements IListVisitor<Course, Integer> {
  Student target;

  MultipleCourses(Student target) {
    this.target = target;
  }

  // Dispatches to either forMt or forCons depending on whether the
  // list of courses is empty or not empty.
  public Integer apply(IList<Course> arg) {
    return arg.accept(this);
  }

  // The student is not in any of the courses because the list is empty.
  public Integer forMt(MtList<Course> arg) {
    return 0;
  }

  // Increments for every course in the course pool that the student is taking
  public Integer forCons(ConsList<Course> arg) {
    if (arg.first.inThisCourse(this.target)) {
      return 1 + new MultipleCourses(this.target).apply(arg.rest);
    }
    else {
      return new MultipleCourses(this.target).apply(arg.rest);
    }
  }
}

//An interface representing a general list.
interface IList<T> {
  // Accepts a visitor and determines if the IList is Mt or Cons
  <R> R accept(IListVisitor<T, R> visitor);
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
  public <R> R accept(IListVisitor<T, R> visitor) {
    return visitor.forCons(this);
  }
}

// A class representing an empty list of courses
class MtList<T> implements IList<T> {
  // Applies the given visitor's forCons method on this empty list.
  public <R> R accept(IListVisitor<T, R> visitor) {
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

  void testingEnroll(Tester t) {
    reset();
    t.checkExpect(this.DWang.courses, new MtList<Course>());
    this.DWang.enroll(Linear);
    t.checkExpect(this.DWang.courses, new ConsList<Course>(this.Linear, new MtList<Course>()));
    this.DWang.enroll(CS2500);
    t.checkExpect(this.DWang.courses,
        new ConsList<Course>(CS2500, new ConsList<Course>(Linear, new MtList<Course>())));
    t.checkExpect(this.Preston.courses, new MtList<Course>());
    this.Preston.enroll(CS2510);
    t.checkExpect(this.Preston.courses, new ConsList<Course>(CS2510, new MtList<Course>()));
    this.Preston.enroll(CS2500);
    t.checkExpect(this.Preston.courses,
        new ConsList<Course>(CS2500, new ConsList<Course>(CS2510, new MtList<Course>())));
  }

  boolean testingSameStudent(Tester t) {
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

  void testingClassmates(Tester t) {
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

  void testingInThisCourse(Tester t) {
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

  // tests dejavu
  void testingDejavuMethod(Tester t) {
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

  // tests for InCourses function object
  void testingInCourseClass(Tester t) {
    reset();
    InCourses emptyCourses = new InCourses(new MtList<Course>());
    InCourses linearCS2510 = new InCourses(
        new ConsList<Course>(this.Linear, new ConsList<Course>(this.CS2510, new MtList<Course>())));
    InCourses cyberLinearCyber2 = new InCourses(
        new ConsList<Course>(this.Cyber, new ConsList<Course>(this.Linear,
            new ConsList<Course>(this.Cyber2, new MtList<Course>()))));

    t.checkExpect(emptyCourses.apply(SR), false);
    t.checkExpect(emptyCourses.apply(DWang), false);
    t.checkExpect(emptyCourses.apply(LiAnn), false);
    t.checkExpect(cyberLinearCyber2.apply(SR), false);
    t.checkExpect(cyberLinearCyber2.apply(DWang), false);
    t.checkExpect(cyberLinearCyber2.apply(LiAnn), false);

    this.DWang.enroll(Linear);

    t.checkExpect(linearCS2510.apply(this.DWang), true);
    t.checkExpect(linearCS2510.apply(SR), false);

    this.SR.enroll(this.Linear);
    t.checkExpect(linearCS2510.apply(SR), true);
    t.checkExpect(linearCS2510.apply(LiAnn), false);
    this.LiAnn.enroll(this.CS2510);
    t.checkExpect(linearCS2510.apply(this.LiAnn), true);
  }

  // tests for InRoster function object
  void testingInRosterClass(Tester t) {
    reset();
    IList<Course> mt = new MtList<Course>();
    IList<Course> linearCS2510 = new ConsList<Course>(this.Linear,
        new ConsList<Course>(this.CS2510, new MtList<Course>()));
    IList<Course> linearCyber2 = new ConsList<Course>(this.Linear,
        new ConsList<Course>(this.Cyber2, new MtList<Course>()));
    IList<Course> cyberLinearCyber2 = new ConsList<Course>(this.Cyber,
        (new ConsList<Course>(this.Linear,
            new ConsList<Course>(this.Cyber2, new MtList<Course>()))));

    InRoster SR = new InRoster(this.SR);
    InRoster DW = new InRoster(this.DWang);

    t.checkExpect(SR.apply(mt), false);
    t.checkExpect(DW.apply(mt), false);
    this.SR.enroll(this.Cyber);
    t.checkExpect(SR.apply(cyberLinearCyber2), true);
    t.checkExpect(DW.apply(cyberLinearCyber2), false);
    this.DWang.enroll(this.Cyber);
    t.checkExpect(DW.apply(cyberLinearCyber2), true);

    t.checkExpect(SR.forMt((MtList<Course>) mt), false);
    t.checkExpect(DW.forMt((MtList<Course>) mt), false);

    t.checkExpect(SR.forCons((ConsList<Course>) cyberLinearCyber2), true);
    t.checkExpect(DW.forCons((ConsList<Course>) cyberLinearCyber2), true);

    this.DWang.enroll(this.CS2510);
    this.SR.enroll(this.CS2510);

    t.checkExpect(SR.forCons((ConsList<Course>) linearCS2510), true);
    t.checkExpect(DW.forCons((ConsList<Course>) linearCS2510), true);

    t.checkExpect(SR.forCons((ConsList<Course>) linearCyber2), false);
    t.checkExpect(DW.forCons((ConsList<Course>) linearCyber2), false);
  }

  // tests for OneOfStudents function object
  void testingOneOfStudentsMethod(Tester t) {
    reset();
    IList<Student> mt = new MtList<Student>();
    IList<Student> Daniel = new ConsList<Student>(this.DWang, mt);
    IList<Student> SrD = new ConsList<Student>(this.SR, Daniel);
    IList<Student> LiAnnSrD = new ConsList<Student>(this.LiAnn, SrD);

    OneOfStudents DW = new OneOfStudents(this.DWang);
    OneOfStudents SR = new OneOfStudents(this.SR);
    OneOfStudents LiAnn = new OneOfStudents(this.LiAnn);
    OneOfStudents Preston = new OneOfStudents(this.Preston);
    OneOfStudents Ethan = new OneOfStudents(this.Ethan);

    t.checkExpect(DW.apply(mt), false);
    t.checkExpect(SR.apply(mt), false);
    t.checkExpect(LiAnn.apply(mt), false);
    t.checkExpect(Preston.apply(mt), false);
    t.checkExpect(Ethan.apply(mt), false);

    t.checkExpect(Ethan.apply(LiAnnSrD), false);
    t.checkExpect(Preston.apply(LiAnnSrD), false);
    t.checkExpect(DW.apply(LiAnnSrD), true);
    t.checkExpect(SR.apply(LiAnnSrD), true);
    t.checkExpect(LiAnn.apply(LiAnnSrD), true);

    t.checkExpect(LiAnn.forMt((MtList<Student>) mt), false);
    t.checkExpect(Preston.forMt((MtList<Student>) mt), false);
    t.checkExpect(Ethan.forMt((MtList<Student>) mt), false);

    t.checkExpect(Ethan.forCons((ConsList<Student>) LiAnnSrD), false);
    t.checkExpect(Preston.forCons((ConsList<Student>) LiAnnSrD), false);
    t.checkExpect(DW.forCons((ConsList<Student>) LiAnnSrD), true);
    t.checkExpect(SR.forCons((ConsList<Student>) LiAnnSrD), true);
    t.checkExpect(LiAnn.forCons((ConsList<Student>) LiAnnSrD), true);
    t.checkExpect(DW.forCons((ConsList<Student>) Daniel), true);
    t.checkExpect(SR.forCons((ConsList<Student>) Daniel), false);
  }

  // tests for MultipleCourses function object
  void testingMultipleCoursesClass(Tester t) {
    reset();
    MtList<Course> mtCourses = new MtList<Course>();
    ConsList<Course> mosesCourses = new ConsList<Course>(this.Linear,
        new ConsList<Course>(this.CS3700, new MtList<Course>()));

    MultipleCourses targetDWang = new MultipleCourses(this.DWang);
    MultipleCourses targetSR = new MultipleCourses(this.SR);
    MultipleCourses targetLiAnn = new MultipleCourses(this.LiAnn);
    MultipleCourses targetPreston = new MultipleCourses(this.Preston);
    MultipleCourses targetEthan = new MultipleCourses(this.Ethan);

    t.checkExpect(targetDWang.apply(mtCourses), 0);
    t.checkExpect(targetSR.apply(mtCourses), 0);
    t.checkExpect(targetLiAnn.apply(mtCourses), 0);
    t.checkExpect(targetPreston.apply(mtCourses), 0);
    t.checkExpect(targetEthan.apply(mtCourses), 0);

    this.DWang.enroll(this.Linear);
    this.DWang.enroll(this.CS3700);

    t.checkExpect(targetDWang.apply(mosesCourses), 2);
    t.checkExpect(targetSR.apply(mosesCourses), 0);
    t.checkExpect(targetLiAnn.apply(mosesCourses), 0);
    t.checkExpect(targetPreston.apply(mosesCourses), 0);
    t.checkExpect(targetEthan.apply(mosesCourses), 0);

    t.checkExpect(targetDWang.forMt(mtCourses), 0);
    t.checkExpect(targetSR.forMt(mtCourses), 0);
    t.checkExpect(targetLiAnn.forMt(mtCourses), 0);
    t.checkExpect(targetPreston.forMt(mtCourses), 0);
    t.checkExpect(targetEthan.forMt(mtCourses), 0);

    t.checkExpect(targetDWang.forCons(mosesCourses), 2);
    t.checkExpect(targetSR.forCons(mosesCourses), 0);
    t.checkExpect(targetLiAnn.forCons(mosesCourses), 0);
    t.checkExpect(targetPreston.forCons(mosesCourses), 0);
    t.checkExpect(targetEthan.forCons(mosesCourses), 0);
  }
}
