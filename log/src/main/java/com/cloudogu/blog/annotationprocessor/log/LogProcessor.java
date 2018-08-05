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
//                TypeElement clazz = (TypeElement) element.getEnclosingElement();
                JavacProcessingEnvironment javacProcessingEnv = (JavacProcessingEnvironment) processingEnv;
                JavacElements utils = javacProcessingEnv.getElementUtils();
                JCTree blockNode = utils.getTree(element);
                // Получение строк кода в методе под аннотацией
                final List<JCTree.JCStatement> statements = ((JCTree.JCMethodDecl) blockNode).body.stats;
//                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "statements: " + statements);

                TreeMaker maker  = TreeMaker.instance(javacProcessingEnv.getContext());
                // Новое тело метода
                List<JCTree.JCStatement> newStatements = List.nil();
                // Добавляем в начало метода сохранение текущего времени
                JCTree.JCExpression exp = maker.Ident(utils.getName("System"));
                String methodName = "nanoTime";
                exp = maker.Select(exp, utils.getName(methodName));
                JCTree.JCExpression currentTime = maker.Apply(List.<JCTree.JCExpression>nil(), exp, List.<JCTree.JCExpression>nil());

                String fieldName_start = "time_start";
                JCTree.JCVariableDecl var_start = maker.VarDef(maker.Modifiers(Flags.FINAL),
                        utils.getName(fieldName_start), maker.TypeIdent(com.sun.tools.javac.code.TypeTag.LONG),
                        currentTime);

                String fieldName_end = "time_end";
                JCTree.JCVariableDecl var_end = maker.VarDef(maker.Modifiers(Flags.FINAL),
                        utils.getName(fieldName_end), maker.TypeIdent(com.sun.tools.javac.code.TypeTag.LONG),
                        currentTime);

                JCTree.JCExpression elapsedTime = maker.Binary(com.sun.tools.javac.tree.JCTree.Tag.MINUS,
                        maker.Ident(var_end.name), maker.Ident(var_start.name));
//
                List<JCTree.JCExpression> printlnArgs = List.nil();
                printlnArgs = printlnArgs.append(elapsedTime);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "printlnArgs: " + printlnArgs);






                JCTree.JCVariableDecl timeStartVar = makeTimeStartVar(maker, utils, log);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "timeStartVar: " + timeStartVar);
                newStatements = newStatements.append(timeStartVar);
//                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "newStatements: " + newStatements);
                // Создаём тело блока try, копируем в него оригинальное содержимое метода
                List<JCTree.JCStatement> tryBlock = List.nil();
//                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "tryBlock: " + tryBlock);
                for (JCTree.JCStatement statement : statements) {
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "statement: " + statement);
                    tryBlock = tryBlock.append(statement);
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "tryBlock: " + tryBlock);
                }

                // Создаём тело блока finally, добавляем в него вывод затраченного времени
                JCTree.JCBlock finalizer = makePrintBlock(maker, utils, log, timeStartVar);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "finalizer: " + finalizer);
                JCTree.JCStatement stat = maker.Try(maker.Block(0, tryBlock), List.<JCTree.JCCatch>nil(), finalizer);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "stat: " + stat);
                newStatements = newStatements.append(stat);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "newStatements: " + newStatements);
                // Заменяем старый код метода на новый
                ((JCTree.JCMethodDecl) blockNode).body.stats = newStatements;

                /*try {
                    JavaFileObject f = processingEnv.getFiler().createSourceFile(clazz.getQualifiedName());
                    Writer w = f.openWriter();
                    PrintWriter pw = new PrintWriter(w);
                    pw.println("l");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Log at " + element);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Log at " + log.name());
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Log at " + log.type());
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Log at " + log.value());
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Log at " + Thread.currentThread().getId());
                System.setProperty("idThread", String.valueOf(Thread.currentThread().getId()));
            }
        }
        return true;
    }

    private JCTree.JCExpression makeCurrentTime(TreeMaker maker, JavacElements utils, Log log) {
        // Создаём вызов System.nanoTime или System.currentTimeMillis
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"maker: " + maker);
        JCTree.JCExpression exp = maker.Ident(utils.getName("System"));
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"exp: " + exp);
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
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"exp: " + exp);

        return maker.Apply(List.<JCTree.JCExpression>nil(), exp, List.<JCTree.JCExpression>nil());
    }

    protected JCTree.JCVariableDecl makeTimeStartVar(TreeMaker maker, JavacElements utils, Log log) {
        // Создаём финальную переменную для хранения времени старта. Имя переменной в виде time_start
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"maker: " + maker);
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"utils: " + utils);
        JCTree.JCExpression currentTime = makeCurrentTime(maker, utils, log);
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"currentTime: " + currentTime);
//        String fieldName = "time_start_" + (int) (Math.random() * 10000);
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"fieldName: " + fieldName);
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

        // Форматируем результат
//        JCTree.JCExpression formatExpression = maker.Ident(utils.getName("String"));
//        formatExpression = maker.Select(formatExpression, utils.getName("format"));
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"formatExpression: " + formatExpression);
        // Собираем все кусочки вместе
        List<JCTree.JCExpression> printArgs = List.nil();
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"printArgs: " + printArgs);
//        printArgs.append(maker.Literal(log.format()));
        printArgs.append(elapsedTime);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"printArgs: " + printArgs);
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"printArgs: " + printArgs);
//        JCTree.JCExpression format = maker.Apply(List.<JCTree.JCExpression>nil(), formatExpression, printArgs);
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"format: " + format);
//        List<JCTree.JCExpression> printlnArgs = List.nil();
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"printlnArgs: " + printlnArgs);
//        printlnArgs.append(format);
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"printlnArgs: " + printArgs);
        JCTree.JCExpression print = maker.Apply(List.<JCTree.JCExpression>nil(), printlnExpression, printArgs);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"print: " + elapsedTime);
        JCTree.JCExpressionStatement stmt = maker.Exec(print);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"stmt: " + stmt);
        List<JCTree.JCStatement> stmts = List.nil();
        stmts.append(stmt);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"stmts: " + stmts);
        return maker.Block(0, stmts);
    }
}