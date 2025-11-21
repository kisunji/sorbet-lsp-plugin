# typed: strict
require 'sorbet-runtime'

class Calculator
  extend T::Sig

  sig { params(a: Integer, b: Integer).returns(Integer) }
  def add(a, b)
    a + b
  end

  sig { params(a: Integer, b: Integer).returns(Integer) }
  def subtract(a, b)
    a - b
  end

  sig { params(a: Numeric, b: Numeric).returns(Numeric) }
  def multiply(a, b)
    a * b
  end

  sig { params(a: Float, b: Float).returns(Float) }
  def divide(a, b)
    if b == 0
      raise ArgumentError, "Cannot divide by zero"
    end
    a / b
  end

  # This method has a type error - returns String instead of Integer
  sig { returns(Integer) }
  def broken_method
    "This is a string, not an integer"  # Type error
  end
end

# Usage
calc = Calculator.new
puts calc.add(5, 3)
puts calc.multiply(2.5, 4.0)

# This should cause a type error
result = calc.divide(10.0, "not a number")  # Type error: wrong argument type
