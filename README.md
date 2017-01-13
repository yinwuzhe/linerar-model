# linerar-model
  通过java生态系统的clojure语言实现的glpk的算法 和一些基本的分配模型；
  首先因为glpk是一个c++的软件，所以想要在clojure里面使用glpk这个强大的线性规划的工具可不是一件轻松的事情，具体请看我的博客文章JNI技术（http://blog.csdn.net/yin_wuzhe/article/details/52562146 有问题可以给我私信，对一些问题和难点我们一起探讨解决。
  其次，什么是线性规划问题，以及选择什么工具来解决这类问题，希望我的另一个文章线性规划和约束满足问题(http://blog.csdn.net/yin_wuzhe/article/details/52563109) 能很好的解给出我的解答。
  最后，我将总结一些使用的基本步骤，和对代码做出必要性的解释。
  1.在你的机器上按照glpk的so库（或者拷贝到相应的目录下面去也可以,比如/usr/local/lib/libglpk.so）
  2.在你的机器上按照libglpk_java的so库（或者拷贝到相应的目录下面去也可以，比如/usr/local/lib/libglpk_java.so）
  # find / -name *glpk.so
  /usr/local/lib/libglpk.so
 /usr/local/lib/libglpk_java.so

  3.在你的clojure项目里面加入[org.gnu.glpk/glpk-java "1.7.0" ]依赖，使得你可以通过java的api方式来调用glpk
  4.编写如glpk_model.clj文件中类似的代码来解决问题
  
  希望你的问题能迎刃而解！
  另外，想要通过glpk解决线性规划的问题固然容易，但是如果你想知其然并且知其所以然，你肯定会说我大学的时候学过线性代数，但是都忘记了:(
  在这里推荐你一个课程：
  学完这个之后
  线性规划问题推荐的几本书：
  
  恩，还有可以继续关注我的博客 或许我的理解（认为）你也会觉得很好 换个角度来看待问题吧：
  ）
