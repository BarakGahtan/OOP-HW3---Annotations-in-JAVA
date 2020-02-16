package Solution;

import Provided.*;
import org.junit.ComparisonFailure;
import java.lang.reflect.*;
import java.util.*;

public class StoryTesterImpl implements StoryTester {
    private Object TEST;
    private String[] sentences;
    private StoryTestExceptionImpl storyException;

    private String[] breakSentenceByDelimiter(String sentence, String delimiter) {
        return sentence.split(delimiter);
    }


//    private void invokeMethod(Method method, String[] sentences, )

    // return an array of strings.
    // should be checked if it is a string or an Int.
    private ArrayList<String> extractParams(Method method, String[] sentences) {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            String parameter = sentences[i].substring(sentences[i].lastIndexOf(" ") + 1);
            result.add(parameter);
        }
        return result;
    }

    private ArrayList<Object> convertParamsToReal(Method method, ArrayList<String> generalParameters) {
        Type paramsType[] = method.getParameterTypes();
        ArrayList<Object> res = new ArrayList<>();
        for (int i = 0; i < paramsType.length; i++) {
            if (paramsType[i] == Integer.class) {
                res.add(Integer.parseInt(generalParameters.get(i)));
            } else {
                res.add(generalParameters.get(i));
            }
        }
        return res;
    }

    private void recognizeGivenMethod(String sentence) throws GivenNotFoundException,
            InvocationTargetException, IllegalAccessException {
        Class<?> currentClass = this.TEST.getClass();
        boolean isFound = false;
        while (!isFound) {
            if (currentClass == null) throw new GivenNotFoundException();
            Method[] classMethods = Arrays.stream(currentClass.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Given.class))
                    .toArray(Method[]::new);
            for (int k=0; k<classMethods.length && !isFound ; k++) {
                String[] annotationsSentences = this.breakSentenceByDelimiter(classMethods[k].getAnnotation(Given.class).value(), " and ");
                String[] storySentences = this.breakSentenceByDelimiter(sentence, " and ");
                String annotation = "Given ";
                String story = "";
                if(annotationsSentences.length==storySentences.length){
                    for (int i = 0; i < annotationsSentences.length; i++) {
                        annotation += annotationsSentences[i].substring(0,annotationsSentences[i].lastIndexOf(" "));
                        story += storySentences[i].substring(0,storySentences[i].lastIndexOf(" "));
                        if (i < annotationsSentences.length - 1) {
                            annotation += " and ";
                            story += " and ";
                        }
                    }
                    if (story.compareTo(annotation) == 0) {
                        isFound = true;
                        ArrayList<String> stringParams = extractParams(classMethods[k], storySentences);
                        ArrayList<Object> convertedParams = convertParamsToReal(classMethods[k], stringParams);
                        classMethods[k].setAccessible(true);
                        classMethods[k].invoke(TEST, convertedParams.toArray());
                    }
                }

            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private void recognizeWhenMethod(String sentence) throws WhenNotFoundException,
            InvocationTargetException, IllegalAccessException {
        Class<?> currentClass = TEST.getClass();
        boolean isFound = false;
        while (!isFound) {
            if (currentClass == null) throw new WhenNotFoundException();
            Method[] classMethods = Arrays.stream(currentClass.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(When.class))
                    .toArray(Method[]::new);
            for (int i = 0; !isFound && i < classMethods.length; i++) {
                // Method m : classMethods)
                String[] annotationsSentences = this.breakSentenceByDelimiter(classMethods[i].getAnnotation(When.class).value(), " and ");
                String[] storySentences = this.breakSentenceByDelimiter(sentence, " and ");
                String annotation = "When ";
                String story = "";
                if(annotationsSentences.length!=storySentences.length){
                    continue;
                }
                for (int j = 0; j < annotationsSentences.length; j++) {
                    annotation += annotationsSentences[j].substring(0, annotationsSentences[j].lastIndexOf(" "));
                    story += storySentences[j].substring(0,storySentences[j].lastIndexOf(" "));
                    if (j < annotationsSentences.length - 1) {
                        annotation += " and ";
                        story += " and ";
                    }
                }
                if (story.compareTo(annotation) == 0) {
                    isFound = true;
                    ArrayList<String> stringParams = extractParams(classMethods[i], storySentences);
                    ArrayList<Object> convertedParams = convertParamsToReal(classMethods[i], stringParams);
                    classMethods[i].setAccessible(true);
                    classMethods[i].invoke(TEST, convertedParams.toArray());
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private boolean recognizeThenMethod(String sentence) throws ThenNotFoundException,
            InvocationTargetException, IllegalAccessException {
        Class<?> currentClass = TEST.getClass();
        boolean isFound = false;
        boolean isSuccess = true;
        StoryTestExceptionImpl currException = new StoryTestExceptionImpl();
        while (!isFound) {
            if (currentClass == null) throw new ThenNotFoundException();
            Method[] classMethods = Arrays.stream(currentClass.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Then.class))
                    .toArray(Method[]::new);
            for (int k=0; k<classMethods.length&&!isFound; k++) {
                String[] orSubSentence = this.breakSentenceByDelimiter(sentence, " or ");
                String[] annotationsSentences = this.breakSentenceByDelimiter(classMethods[k].getAnnotation(Then.class).value(), " and ");
                String[] storySentences = this.breakSentenceByDelimiter(orSubSentence[0], " and ");

                String annotation = "Then ";
                String story = "";
                if(annotationsSentences.length!=storySentences.length){
                    continue;
                }
                for (int i = 0; i < annotationsSentences.length; i++) {
                    if(i>0){
                        annotation+=" and ";
                        story += " and ";
                    }
                    annotation += annotationsSentences[i];
                    annotation= annotation.substring(0, annotation.lastIndexOf(" "));
                    story += storySentences[i];
                    story= story.substring(0,story.lastIndexOf(" "));
                }
                if (story.compareTo(annotation) == 0) {
                    isFound = true;
                    int failCounter = 0;
                    for (int i = 0; i < orSubSentence.length; i++) {
                        storySentences = this.breakSentenceByDelimiter(orSubSentence[i], " and ");
                        ArrayList<String> stringParams = extractParams(classMethods[k], storySentences);
                        ArrayList<Object> convertedParams = convertParamsToReal(classMethods[k], stringParams);
                        classMethods[k].setAccessible(true);
                        try {
                            classMethods[k].invoke(TEST, convertedParams.toArray());
                            break;
                        } catch (InvocationTargetException e) {
                            failCounter++;
                            // check if this is the first fail sentence
                            if (storyException.getNumFail() == 0) {
                                currException.setFirstSentence(sentence);
                                ComparisonFailure failCause = (ComparisonFailure) e.getCause();
                                currException.addExpectedParam(failCause.getExpected());
                                currException.addFoundParam(failCause.getActual());
                            }
                        }
                    }
                    if (failCounter == orSubSentence.length) {
                        if (storyException.getNumFail() == 0) {
                            storyException = currException;
                        }
                        storyException.incNumFailures();
                        isSuccess = false;
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return isSuccess;
    }


    private Method getCloneMethod(Object obj) {
        if (obj == null) return null;
        if (!(obj instanceof Cloneable)) return null;
        try {
            Method clone = obj.getClass().getDeclaredMethod("clone");
            return clone;
        } catch (NoSuchMethodException exp) {
            return null;
        }
    }


    private Map<Field, Object> backupTestObj(Object obj) {
        Map<Field, Object> backupFields = new HashMap<>();
        for (Field f : obj.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object val = f.get(obj);
                if (val == null) {
                    continue;
                }
                Method clone = this.getCloneMethod(val);
                if (clone != null) {
                    clone.setAccessible(true);
                    backupFields.put(f, clone.invoke(val));
                } else {
                    //the object is not cloneable, try use the copy constructor if
                    try {
                        Constructor<?> copyConstructor = val.getClass().getDeclaredConstructor(val.getClass());
                        copyConstructor.setAccessible(true);
                        backupFields.put(f, copyConstructor.newInstance(val));
                    } catch (NoSuchMethodException | InstantiationException exp) {
                        //there is no clone or copy c'tor
                        backupFields.put(f, val);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException exp) {
                //do nothing
            }
        }
        return backupFields;
    }

    private void restorFields(Map<Field, Object> backupFields) {
        backupFields.entrySet().forEach(tupple -> {
            tupple.getKey().setAccessible(true);
            try {
                tupple.getKey().set(TEST, tupple.getValue());
            } catch (IllegalAccessException exp) {
                // do nothing
            }
        });
    }


    private void testOnInheritanceTreeAux(String story) throws Exception {
        this.storyException = new StoryTestExceptionImpl();
        sentences = story.split("\n");
        //Given must be the first sentence
        try {
            recognizeGivenMethod(sentences[0]);
        } catch (GivenNotFoundException exp) {
            throw exp;
        }
        Map<Field, Object> backupFields = null;
        boolean isBackupExists = false;
        boolean isThenSentenceFailed = false;
        // accept only When and Then sentences
        for (int i = 1; i < sentences.length; i++) {
            if (sentences[i].startsWith("Then")) {
                isBackupExists = false;
                //this sentence is a Then sentence
                try {
                    if (!recognizeThenMethod(sentences[i])) {
                        //do restore from backup
                        restorFields(backupFields);
                        isThenSentenceFailed = true;
                    }
                } catch (ThenNotFoundException exp) {
                    throw exp;
                }
            } else if (sentences[i].startsWith("When")) {
                if (!isBackupExists) {
                    backupFields = backupTestObj(TEST);
                    isBackupExists = true;
                }
                try {
                    recognizeWhenMethod(sentences[i]);
                } catch (WhenNotFoundException exp) {
                    throw exp;
                }
            }
        }
        if (isThenSentenceFailed) {
            throw storyException;
        }
    }

    private boolean isGivenPresents(Class<?> checkedClass, String firstSentence) {
        Class<?> currentClass = checkedClass;
        while (currentClass != null) {
            //t0do : out function recognizeGivenMethod
            Method[] classMethods = Arrays.stream(currentClass.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Given.class))
                    .toArray(Method[]::new);
            for (Method m : classMethods) {
                String[] annotationsSentences = this.breakSentenceByDelimiter(m.getAnnotation(Given.class).value(), " and ");
                String[] storySentences = this.breakSentenceByDelimiter(firstSentence, " and ");
                String annotation = "Given ";
                String story = "";
                if (annotationsSentences.length==storySentences.length) {
                    for (int i = 0; i < annotationsSentences.length; i++) {
                        annotation += annotationsSentences[i].substring(0, annotationsSentences[i].lastIndexOf(" "));
                        story += storySentences[i].substring(0, storySentences[i].lastIndexOf(" "));
                        if (i < annotationsSentences.length - 1) {
                            annotation += " and ";
                            story += " and ";
                        }
                    }
                    if (annotation.compareTo(story) == 0) {
                        return true;
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }


    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        if (story == null || testClass == null) {
            throw new IllegalArgumentException();
        }
        this.TEST = testClass.getDeclaredConstructor().newInstance();
        this.testOnInheritanceTreeAux(story);
    }

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        if (story == null || testClass == null) {
            throw new IllegalArgumentException();
        }
        String firstSentence = story.split("\n")[0];

        // class we already checked in them
        ArrayList<Class<?>> checkedClasses = new ArrayList<>();
        checkedClasses.add(testClass);

        // searchClasses
        Deque<Class<?>> classStack = new ArrayDeque<>();
        classStack.push(testClass);

        Deque<Constructor<?>> ctors = new ArrayDeque<>();
        ctors.push(testClass.getConstructor());

        ArrayList<Object> fatherClasses = new ArrayList<>();
        fatherClasses.add(null);

        while (!classStack.isEmpty()) {
            Class<?> currentClass = classStack.pop();
            Constructor<?> ctor = ctors.pop();
            Object fatherClass = fatherClasses.remove(0);

            if (isGivenPresents(currentClass, firstSentence)) {
                if (fatherClass == null) {
                    this.TEST = currentClass.getConstructor().newInstance();
                } else {
                    this.TEST = ctor.newInstance(fatherClass);
                }
                testOnInheritanceTreeAux(story);
                return;
            }

            for (Class<?> innerClass : currentClass.getClasses()) {
                if (checkedClasses.contains(innerClass)) {
                    continue;
                }

                classStack.push(innerClass);
                checkedClasses.add(innerClass);
                if (fatherClass == null) {
                    fatherClasses.add(0, ctor.newInstance());
                } else {
                    fatherClasses.add(0, ctor.newInstance(fatherClass));
                }
                ctors.push(innerClass.getConstructors()[0]);
            }
        }
        throw new GivenNotFoundException();
    }
}
