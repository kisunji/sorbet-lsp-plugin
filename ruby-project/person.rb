# typed: true
require 'sorbet-runtime'

class Person
  extend T::Sig

  sig { params(name: String, age: Integer).void }
  def initialize(name, age)
    @name = name
    @age = age
  end

  sig { returns(String) }
  attr_reader :name

  sig { returns(Integer) }
  attr_reader :age

  sig { returns(String) }
  def greeting
    "Hello, my name is #{@name} and I am #{@age} years old."
  end

  sig { params(other: Person).returns(T::Boolean) }
  def older_than?(other)
    @age > other.age
  end
end

# This should show a type error - passing wrong types
person1 = Person.new("Alice", 30)
person2 = Person.new("Bob", "not a number")  # Type error: should be Integer

puts person1.greeting
puts person1.older_than?(person2)
