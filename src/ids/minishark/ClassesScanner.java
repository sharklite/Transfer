package ids.minishark;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class ClassesScanner {

    static Set<Class<?>> getClasses(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        String aClassName=packageName;
        // 获取包的名字 并进行替换
        packageName=packageName.replace(".*","");
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径  file类型的扫描
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findClassesInPackage(packageName,filePath, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件 jar类型的扫描
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1)) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class")&& !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.'+ className));
                                        } catch (ClassNotFoundException e) {
                                            // error("添加用户自定义视图类错误 找不到此类的.class文件");
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        // ("在扫描用户定义视图时从jar包获取文件出错");
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(classes.isEmpty()){
            try {
                classes.add(Thread.currentThread().getContextClassLoader().loadClass(aClassName));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }


    private static void findClassesInPackage(String packageName,String packagePath,Set<Class<?>> classes){
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirFiles = dir.listFiles(new FileFilter(){
            // 自定义过滤规则 可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file){
                return (file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        if(dirFiles==null)
            return;
        // 循环所有文件
        for (File file : dirFiles){
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findClassesInPackage(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
            }else{
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try{
                    // 添加到集合中
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args){
//        Set<Class<?>> set=ClassesScanner.getClasses("com.microsoft.*");
        Set<Class<?>> set=ClassesScanner.getClasses("test");
        for(Class<?> c:set){
            System.out.println(c+" , "+c.getCanonicalName());
        }
        System.out.println(" ------------- ");
    }
}
