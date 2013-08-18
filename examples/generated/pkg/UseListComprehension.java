package pkg;

import java.util.List;
public class UseListComprehension
{
  public static void main(String[] args)
  {
    // [ "a", "b", "c" ]
    System.out.println((new my_util.ListComprehension<String>() {
      @Override public java.util.List<String> getList() {
        java.util.List<String> list = new java.util.ArrayList<>();
        for (String x : new String[]{ "a", "b", "c" }) {
  	    list.add(x); } return list;
      }
    }.getList()));

    // [ "a", "b", "c" ]
    System.out.println((new my_util.ListComprehension<String>() {
      @Override public java.util.List<String> getList() {
        java.util.List<String> list = new java.util.ArrayList<>();
        for (String x : new String[]{ "a", "", "b", "c", "" }) {
  	    if (!x.isEmpty()) { list.add(x); } } return list;
      }
    }.getList()));

    // [ad, bd, cd, ae, be, ce, af, bf, cf]
    System.out.println((new my_util.ListComprehension<String>() {
      @Override public java.util.List<String> getList() {
        java.util.List<String> list = new java.util.ArrayList<>();
        for (String y : new String[]{ "d", "e", "f"}) {
  	    for (String x : new String[]{ "a", "b", "c"}) {
  	    list.add(x + y); } } return list;
      }
    }.getList()));

    // [ad, bd, cd, ae, be, ce, af, bf, cf]
    System.out.println((new my_util.ListComprehension<String>() {
      @Override public java.util.List<String> getList() {
        java.util.List<String> list = new java.util.ArrayList<>();
        for (String y : new String[]{ "d", "", "e", "f", "" }) {
  	    if (!y.isEmpty()) { for (String x : new String[]{ "a", "", "b", "c", "" }) {
  	    if (!x.isEmpty()) { list.add(x + y); } } } } return list;
      }
    }.getList()));
  }
}
