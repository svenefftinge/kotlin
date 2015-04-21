interface Test<T> {

    default T testDefault(T p) {
        return p;
    }
}
