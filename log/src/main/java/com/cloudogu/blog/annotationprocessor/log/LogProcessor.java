package com.cloudogu.blog.annotationprocessor.log;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("com.cloudogu.blog.annotationprocessor.log.Log")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class LogProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                Log log = element.getAnnotation(Log.class);
                JavacProcessingEnvironment javacProcessingEnv = (JavacProcessingEnvironment) processingEnv;
                JavacElements utils = javacProcessingEnv.getElementUtils();
                JCTree blockNode = utils.getTree(element);

                TreeMaker maker  = TreeMaker.instance(javacProcessingEnv.getContext());

                // Новое тело метода
                List<JCTree.JCStatement> newStatements = List.nil();

//                String fieldName_start = "time_start";
//                JCTree.JCVariableDecl var_start = maker.VarDef(maker.Modifiers(Flags.FINAL),
//                        utils.getName(fieldName_start), maker.TypeIdent(com.sun.tools.javac.code.TypeTag.LONG),
//                        currentTime);
//
//                String fieldName_end = "time_end";
//                JCTree.JCVariableDecl var_end = maker.VarDef(maker.Modifiers(Flags.FINAL),
//                        utils.getName(fieldName_end), maker.TypeIdent(com.sun.tools.javac.code.TypeTag.LONG),
//                        currentTime);
//
//                JCTree.JCExpression elapsedTime = maker.Binary(com.sun.tools.javac.tree.JCTree.Tag.MINUS,
//                        maker.Ident(var_end.name), maker.Ident(var_start.name));
//
//                List<JCTree.JCExpression> printlnArgs = List.nil();
//                printlnArgs = printlnArgs.append(elapsedTime);

                // Добавляем в начало метода сохранение текущего времени
                JCTree.JCVariableDecl timeStartVar = makeTimeStartVar(maker, utils, log);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "timeStartVar: " + timeStartVar);
                newStatements = newStatements.append(timeStartVar);
                // Создаём тело блока try, копируем в него оригинальное содержимое метода
                List<JCTree.JCStatement> tryBlock = List.nil();
                // Получение строк кода в методе под аннотацией
                final List<JCTree.JCStatement> statements = ((JCTree.JCMethodDecl) blockNode).body.stats;
                for (JCTree.JCStatement statement : statements) {
                    tryBlock = tryBlock.append(statement);
                }
                JCTree.JCBlock tryBl = maker.Block(0, tryBlock);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "tryBl: " + tryBl);
                // Создаём тело блока finally, добавляем в него вывод затраченного времени
                JCTree.JCBlock finalizer = makePrintBlock(maker, utils, log, timeStartVar);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "finalizer: " + finalizer);
                JCTree.JCStatement stat = maker.Try(tryBl, List.<JCTree.JCCatch>nil(), finalizer);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "stat: " + stat);
                newStatements = newStatements.append(stat);

//                JCTree.JCExpression currentTime = makeCurrentTime(maker, utils, log);
//                JCTree.JCExpression elapsedTime = maker.Binary(com.sun.tools.javac.tree.JCTree.Tag.MINUS, currentTime, maker.Ident(timeStartVar.name));
//                JCTree.JCVariableDecl resultTime = maker.VarDef(maker.Modifiers(Flags.FINAL), utils.getName("resultTime"), maker.TypeIdent(com.sun.tools.javac.code.TypeTag.LONG), elapsedTime);
//                newStatements = newStatements.append(resultTime);

                // Заменяем старый код метода на новый
                ((JCTree.JCMethodDecl) blockNode).body.stats = newStatements;

                // Создание java файла
                TypeElement clazz = (TypeElement) element.getEnclosingElement();
                String pack = clazz.getQualifiedName().toString();
                try {
                    JavaFileObject f = processingEnv.getFiler().createSourceFile(clazz.getQualifiedName() + "Autogenerate");
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Creating " + f.toUri());
                    try (Writer w = f.openWriter()) {
                        PrintWriter pw = new PrintWriter(w);
                        pw.println("package " + pack.substring(0, pack.lastIndexOf('.')) + ";");
                        pw.println("\npublic class " + clazz.getSimpleName() + "Autogenerate {");
                        pw.println("}");
                        pw.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Log at " + element);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Log at " + log.name());
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Log at " + log.type());
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Log at " + log.value());
            }
        }
        return true;
    }

    private JCTree.JCExpression makeCurrentTime(TreeMaker maker, JavacElements utils, Log log) {
        // Создаём вызов System.nanoTime или System.currentTimeMillis
        JCTree.JCExpression exp = maker.Ident(utils.getName("System"));
        String methodName;
        switch (log.interval()) {
            case NANOSECOND:
                methodName = "nanoTime";
                break;
            default:
                methodName = "currentTimeMillis";
                break;
        }
        exp = maker.Select(exp, utils.getName(methodName));
        return maker.Apply(List.<JCTree.JCExpression>nil(), exp, List.<JCTree.JCExpression>nil());
    }

    protected JCTree.JCVariableDecl makeTimeStartVar(TreeMaker maker, JavacElements utils, Log log) {
        // Создаём финальную переменную для хранения времени старта. Имя переменной в виде time_start
        JCTree.JCExpression currentTime = makeCurrentTime(maker, utils, log);
        return maker.VarDef(maker.Modifiers(Flags.FINAL), utils.getName("start_time"), maker.TypeIdent(com.sun.tools.javac.code.TypeTag.LONG), currentTime);
    }

    protected JCTree.JCBlock makePrintBlock(TreeMaker maker, JavacElements utils, Log log, JCTree.JCVariableDecl var) {
        // Создаём вызов System.out.println
        JCTree.JCExpression printlnExpression = maker.Ident(utils.getName("System"));
        printlnExpression = maker.Select(printlnExpression, utils.getName("out"));
        printlnExpression = maker.Select(printlnExpression, utils.getName("println"));

        // Создаём блок вычисления затраченного времени (currentTime - startTime)
        JCTree.JCExpression currentTime = makeCurrentTime(maker, utils, log);
        JCTree.JCExpression elapsedTime = maker.Binary(com.sun.tools.javac.tree.JCTree.Tag.MINUS, currentTime, maker.Ident(var.name));
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"elapsedTime: " + elapsedTime);

        // Собираем все кусочки вместе
        List<JCTree.JCExpression> printArgs = List.nil();
        printArgs = printArgs.append(elapsedTime);
        JCTree.JCExpression print = maker.Apply(List.<JCTree.JCExpression>nil(), printlnExpression, printArgs);

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"print: " + print);
        JCTree.JCExpressionStatement stmt = maker.Exec(print);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"stmt: " + stmt);
        List<JCTree.JCStatement> stmts = List.nil();
        stmts = stmts.append(stmt);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"stmts: " + stmts);
        return maker.Block(0, stmts);
    }
}