package com.zemnitskiy.visitor;

public interface Result {
   void accept(ResultVisitor v);
}