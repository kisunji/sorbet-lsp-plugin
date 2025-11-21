# typed: false
# This file has no type checking - Sorbet will ignore it

def untyped_method(x, y)
  x + y
end

# No errors will be reported here, even though types are mixed
result = untyped_method(5, "hello")
puts result
