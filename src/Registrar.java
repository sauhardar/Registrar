class Course {
  String name;
  Instructor prof;
  IList<Student> students;

  Course(String name, Instructor prof) { // Courses must have an instructor.
    prof.courses = new ConsList<Course>(this, prof.courses); // not sure here.
    this.name = name;
    this.prof = prof;
    this.students = new MtList<Student>(); // Courses initially have no students taking it. `Not
                                           // sure`
  }
  
  // determines if the given student is in the list of students for this course
  boolean containsStudent(Student s) {
    return new InRoster(s).apply(this);
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
  }

  // Determines if the given student is in any of the same classes as THIS student
  boolean classmates(Student target) {
    return new IsClassmate(target).apply(this.courses);
  }
}

interface IFunc<A, R> {
  R apply(A arg);
}

interface IPred<X> extends IFunc<X, Boolean> { }

interface IListVisitor<T, R> extends IFunc<T, R> {
  R forMt(MtList<T> arg);

  R forCons(ConsList<T> arg);
}

class IsClassmate implements IPred<Student> {
  Student target;
  
  IsClassmate(Student target) {
    this.target = target;
  }
  
  public Boolean apply(Student given) {
    return new InClass(this.target).apply(given);
  }
}

class InClass implements IListVisitor<Student, Boolean> {
  Student target;
  
  InClass(Student target) {
    this.target = target;
  }
  
  public Boolean apply(Student arg) {
    return arg.courses.accept(this);
  }

  public Boolean forMt(MtList<Course> arg) {
    return false;
  }

  public Boolean forCons(ConsList<Course> arg) {
    return new InRoster(this.target).apply((Course) arg.first) || new InClass(this.target).apply(arg.rest); // casting
  }
}

class InRoster implements IListVisitor<Course, Boolean> {
  Student target;
  
  InRoster(Student target) {
    this.target=target;
  }

  public Boolean apply(Course arg) {
    return arg.students.accept(this);
  }

  public Boolean forMt(MtList<Course> arg) {
    return false;
  }

  public Boolean forCons(ConsList<Course> arg) {
    return arg.first.sameName(this.target) || new InRoster(this.target).apply(arg.rest);
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
