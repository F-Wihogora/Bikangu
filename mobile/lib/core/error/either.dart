sealed class Either<L, R> {
  const Either();

  T fold<T>(T Function(L left) ifLeft, T Function(R right) ifRight) {
    if (this is Left<L, R>) {
      return ifLeft((this as Left<L, R>).value);
    } else {
      return ifRight((this as Right<L, R>).value);
    }
  }

  bool get isLeft => this is Left<L, R>;
  bool get isRight => this is Right<L, R>;

  L? get left => isLeft ? (this as Left<L, R>).value : null;
  R? get right => isRight ? (this as Right<L, R>).value : null;
}

class Left<L, R> extends Either<L, R> {
  final L value;
  const Left(this.value);
}

class Right<L, R> extends Either<L, R> {
  final R value;
  const Right(this.value);
}

extension EitherExtension<L, R> on Either<L, R> {
  Either<L, R2> map<R2>(R2 Function(R value) f) {
    return fold(
      (l) => Left(l),
      (r) => Right(f(r)),
    );
  }

  Either<L, R2> flatMap<R2>(Either<L, R2> Function(R value) f) {
    return fold(
      (l) => Left(l),
      (r) => f(r),
    );
  }
}