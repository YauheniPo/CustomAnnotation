package epam.popovich.annotation.time;

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
import java.util.Set;

@SupportedAnnotationTypes("epam.popovich.annotation.time.TrackTime")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TrackTimeProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                TrackTime trackTime = element.getAnnotation(TrackTime.class);
                JavacProcessingEnvironment javacProcessingEnv = (JavacProcessingEnvironment) processingEnv;
                JavacElements utils = javacProcessingEnv.getElementUtils();
                JCTree blockNode = utils.getTree(element);

                TreeMaker maker = TreeMaker.instance(javacProcessingEnv.getContext());

                // Новое тело метода
                List<JCTree.JCStatement> newStatements = List.nil();

                // Добавляем в начало метода сохранение текущего времени
                JCTree.JCVariableDecl timeStartVar = makeTimeStartVar(maker, utils, trackTime);
                newStatements = newStatements.append(timeStartVar);
                // Создаём тело блока try, копируем в него оригинальное содержимое метода
                List<JCTree.JCStatement> tryBlock = List.nil();
                // Получение строк кода в методе под аннотацией
                final List<JCTree.JCStatement> statements = ((JCTree.JCMethodDecl) blockNode).body.stats;
                for (JCTree.JCStatement statement : statements) {
                    tryBlock = tryBlock.append(statement);
                }
                JCTree.JCBlock tryBl = maker.Block(0, tryBlock);
                // Создаём тело блока finally, добавляем в него вывод затраченного времени
                JCTree.JCBlock finalizer = makePrintBlock(maker, utils, trackTime, timeStartVar);
                JCTree.JCStatement stat = maker.Try(tryBl, List.nil(), finalizer);
                newStatements = newStatements.append(stat);

                // Заменяем старый код метода на новый
                ((JCTree.JCMethodDecl) blockNode).body.stats = newStatements;
            }
        }
        return true;
    }

    private JCTree.JCExpression makeCurrentTime(TreeMaker maker, JavacElements utils, TrackTime trackTime) {
        // Создаём вызов System.nanoTime или System.currentTimeMillis
        JCTree.JCExpression exp = maker.Ident(utils.getName("System"));
        String methodName;
        switch (trackTime.interval()) {
            case NANOSECOND:
                methodName = "nanoTime";
                break;
            default:
                methodName = "currentTimeMillis";
                break;
        }
        exp = maker.Select(exp, utils.getName(methodName));
        return maker.Apply(List.nil(), exp, List.nil());
    }

    private JCTree.JCVariableDecl makeTimeStartVar(TreeMaker maker, JavacElements utils, TrackTime trackTime) {
        // Создаём финальную переменную для хранения времени старта. Имя переменной в виде time_start
        JCTree.JCExpression currentTime = makeCurrentTime(maker, utils, trackTime);
        return maker.VarDef(maker.Modifiers(Flags.FINAL), utils.getName("start_time"), maker.TypeIdent(com.sun.tools.javac.code.TypeTag.LONG), currentTime);
    }

    private JCTree.JCBlock makePrintBlock(TreeMaker maker, JavacElements utils, TrackTime trackTime, JCTree.JCVariableDecl var) {
        // Создаём вызов System.out.println
        JCTree.JCExpression printlnExpression = maker.Ident(utils.getName("System"));
        printlnExpression = maker.Select(printlnExpression, utils.getName("out"));
        printlnExpression = maker.Select(printlnExpression, utils.getName("println"));

        // Создаём блок вычисления затраченного времени (currentTime - startTime)
        JCTree.JCExpression currentTime = makeCurrentTime(maker, utils, trackTime);
        JCTree.JCExpression elapsedTime = maker.Binary(com.sun.tools.javac.tree.JCTree.Tag.MINUS, currentTime, maker.Ident(var.name));

        // Собираем все кусочки вместе
        List<JCTree.JCExpression> printArgs = List.nil();
        printArgs = printArgs.append(elapsedTime);
        JCTree.JCExpression print = maker.Apply(List.nil(), printlnExpression, printArgs);

        JCTree.JCExpressionStatement stmt = maker.Exec(print);
        List<JCTree.JCStatement> stmts = List.nil();
        stmts = stmts.append(stmt);
        return maker.Block(0, stmts);
    }
}