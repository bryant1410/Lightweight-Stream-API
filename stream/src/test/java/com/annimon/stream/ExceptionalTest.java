package com.annimon.stream;

import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.ThrowableFunction;
import com.annimon.stream.function.ThrowableSupplier;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests {@code Exceptional}.
 * 
 * @see com.annimon.stream.Exceptional
 */
public class ExceptionalTest {
    
    @Test
    public void testGet() {
        int value = Exceptional
                .of(tenSupplier)
                .get();
        assertEquals(10, value);
    }
    
    @Test
    public void testGetOrElse() {
        int value = Exceptional
                .of(ioExceptionSupplier)
                .getOrElse(20);
        assertEquals(20, value);

        value = Exceptional
                .of(tenSupplier)
                .getOrElse(20);
        assertEquals(10, value);
    }
    
    @Test
    public void testGetOptional() {
        Optional<Integer> value = Exceptional
                .of(ioExceptionSupplier)
                .getOptional();
        assertFalse(value.isPresent());
    }
    
    @Test
    public void testGetException() {
        Throwable throwable = Exceptional
                .of(ioExceptionSupplier)
                .getException();
        assertThat(throwable, instanceOf(IOException.class));
    }
    
    
    @Test
    public void testGetOrThrowWithoutException() throws Throwable {
        int value = Exceptional
                .of(tenSupplier)
                .getOrThrow();
        assertEquals(10, value);
    }
    
    @Test(expected = IOException.class)
    public void testGetOrThrowWithException() throws Throwable {
        Exceptional
                .of(ioExceptionSupplier)
                .getOrThrow();
    }
    
    @Test
    public void testGetOrThrowRuntimeExceptionWithoutException() throws Throwable {
        int value = Exceptional
                .of(tenSupplier)
                .getOrThrowRuntimeException();
        assertEquals(10, value);
    }
    
    @Test(expected = RuntimeException.class)
    public void testGetOrThrowRuntimeExceptionWithException() throws Throwable {
        Exceptional
                .of(ioExceptionSupplier)
                .getOrThrowRuntimeException();
    }
    
    @Test
    public void testGetOrThrowNewExceptionWithoutException() throws Throwable {
        int value = Exceptional
                .of(tenSupplier)
                .getOrThrow(new ArithmeticException());
        assertEquals(10, value);
    }
    
    @Test(expected = ArithmeticException.class)
    public void testGetOrThrowNewExceptionWithException() throws Throwable {
        Exceptional
                .of(ioExceptionSupplier)
                .getOrThrow(new ArithmeticException());
    }
    
    @Test
    public void testGetOrThrowNewExceptionTestCause() {
        try {
            Exceptional
                    .of(ioExceptionSupplier)
                    .getOrThrow(new ArithmeticException());
        } catch (ArithmeticException ae) {
            assertThat(ae.getCause(), instanceOf(IOException.class));
        }
    }

    @Test
    public void testMapWithoutException() {
        String value = Exceptional
                .of(tenSupplier)
                .map(new ThrowableFunction<Integer, String, Throwable>() {
                    @Override
                    public String apply(Integer value) throws Throwable {
                        return Integer.toString(value);
                    }
                })
                .get();
        assertEquals("10", value);
    }

    @Test(expected = NumberFormatException.class)
    public void testMapWithException() throws Throwable {
        Exceptional
                .of(tenSupplier)
                .map(new ThrowableFunction<Integer, String, Throwable>() {
                    @Override
                    public String apply(Integer value) throws Throwable {
                        throw new NumberFormatException();
                    }
                })
                .getOrThrow();
    }

    @Test(expected = NullPointerException.class)
    public void testMapOnNullFunction() throws Throwable {
        Exceptional
                .of(tenSupplier)
                .map(null)
                .getOrThrow();
    }

    @Test(expected = IOException.class)
    public void testMapOnAlreadyFailedExceptional() throws Throwable {
        Exceptional
                .of(ioExceptionSupplier)
                .map(new ThrowableFunction<Integer, String, Throwable>() {
                    @Override
                    public String apply(Integer value) throws Throwable {
                        return Integer.toString(value);
                    }
                })
                .getOrThrow();
    }

    @Test
    public void testIfException() {
        for (final ExceptionType type : ExceptionType.values()) {
            Exceptional
                    .of(new ThrowableSupplier<Integer, Throwable>() {
                        @Override
                        public Integer get() throws Throwable {
                            throwException(type);
                            return 10;
                        }
                    })
                    .ifException(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable value) {
                            assertEquals(type.getException(), value);
                        }
                    });
        }
    }
    
    @Test
    public void testIfExceptionIs() {
        final int INTERRUPTED = 0;
        final int EXCEPTION = 1;
        final int FILE_NOT_FOUND = 2;
        final boolean[] data = new boolean[3];
        
        Exceptional
                .of(new ThrowableSupplier<Integer, Throwable>() {
                    @Override
                    public Integer get() throws Throwable {
                        throwException(ExceptionType.INTERRUPTED);
                        return 10;
                    }
                })
                .ifExceptionIs(InterruptedException.class, new Consumer<InterruptedException>() {
                    @Override
                    public void accept(InterruptedException value) {
                        data[INTERRUPTED] = true;
                    }
                })
                .ifExceptionIs(Exception.class, new Consumer<Exception>() {
                    @Override
                    public void accept(Exception value) {
                        data[EXCEPTION] = true;
                    }
                })
                .ifExceptionIs(FileNotFoundException.class, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable value) {
                        data[FILE_NOT_FOUND] = true;
                    }
                });

        assertTrue(data[INTERRUPTED]);
        assertTrue(data[EXCEPTION]);
        assertFalse(data[FILE_NOT_FOUND]);
    }
    
    @Test
    public void testEqualsReflexive() {
        final Exceptional<Integer> ten1 = Exceptional.of(tenSupplier);
        assertTrue(ten1.equals(ten1));
    }
    
    @Test
    public void testEqualsSymmetric() {
        final Exceptional<Integer> ten1 = Exceptional.of(tenSupplier);
        final Exceptional<Integer> ten2 = Exceptional.of(tenSupplier);
        
        assertTrue(ten1.equals(ten2));
        assertTrue(ten2.equals(ten1));
    }
    
    @Test
    public void testEqualsTransitive() {
        final Exceptional<Integer> ten1 = Exceptional.of(tenSupplier);
        final Exceptional<Integer> ten2 = Exceptional.of(tenSupplier);
        final Exceptional<Integer> ten3 = Exceptional.of(tenSupplier);
        
        assertTrue(ten1.equals(ten2));
        assertTrue(ten2.equals(ten3));
        assertTrue(ten1.equals(ten3));
    }
    
    @Test
    public void testEqualsWithDifferentTypes() {
        final Exceptional<Integer> ten1 = Exceptional.of(tenSupplier);
        assertFalse(ten1.equals(10));
    }
    
    @Test
    public void testEqualsWithDifferentNumberTypes() {
        final Exceptional<Integer> ten1 = Exceptional.of(tenSupplier);
        final Exceptional<Byte> tenByte = Exceptional.of(new ThrowableSupplier<Byte, Throwable>() {
            @Override
            public Byte get() throws Throwable {
                return (byte) 10;
            }
        });
        
        assertFalse(ten1.equals(tenByte));
    }

    @Test
    public void testEqualsWithDifferentExceptions() {
        final Exceptional<Integer> ten1 = Exceptional.of(tenSupplier);
        final Exceptional<Integer> ten2 = Exceptional.of(new ThrowableSupplier<Integer, Throwable>() {
            @Override
            public Integer get() throws Throwable {
                throwIO();
                return 10;
            }
        });

        assertFalse(ten1.equals(ten2));
    }
    
    @Test
    public void testEqualsWithDifferentGenericTypes() {
        final Exceptional<Integer> ten = Exceptional.of(tenSupplier);
        final Exceptional<Integer> io = Exceptional.of(ioExceptionSupplier);
        
        assertFalse(ten.equals(io));
    }
    
    
    @Test
    public void testHashCodeWithSameObject() {
        final Exceptional<Integer> ten1 = Exceptional.of(tenSupplier);
        final Exceptional<Integer> ten2 = Exceptional.of(tenSupplier);
        
        int initial = ten1.hashCode();
        assertEquals(initial, ten1.hashCode());
        assertEquals(initial, ten1.hashCode());
        assertEquals(initial, ten2.hashCode());
    }
    
    @Test
    public void testHashCodeWithDifferentGenericType() {
        final Exceptional<Byte> tenByte = Exceptional.of(new ThrowableSupplier<Byte, Throwable>() {
            @Override
            public Byte get() throws Throwable {
                return (byte) 10;
            }
        });
        final Exceptional<Integer> io = Exceptional.of(ioExceptionSupplier);
        assertNotEquals(io.hashCode(), tenByte.hashCode());
    }
    
    @Test
    public void testToStringWithoutException() {
        assertEquals("Exceptional value 10", Exceptional.of(tenSupplier).toString());
    }
    
    @Test
    public void testToStringWithException() {
        assertEquals("Exceptional throwable java.io.IOException", Exceptional.of(ioExceptionSupplier).toString());
    }
    
    
    private static final ThrowableSupplier<Integer, Throwable> tenSupplier
            = new ThrowableSupplier<Integer, Throwable>() {
        @Override
        public Integer get() throws IOException {
            return 10;
        }
    };
    
    private static final ThrowableSupplier<Integer, Throwable> ioExceptionSupplier
            = new ThrowableSupplier<Integer, Throwable>() {
        @Override
        public Integer get() throws Throwable {
            throwIO();
            return 10;
        }
    };
    
    private static void throwException(ExceptionType type) throws Exception {
        throw type.getException();
    }
    
    private static void throwIO() throws IOException {
        throw new IOException();
    }
    
    private static enum ExceptionType {
        NULL_POINTER(new NullPointerException()),
        UNSUPPORTED_OPERATION(new UnsupportedOperationException()),
        FILE_NOT_FOUND(new FileNotFoundException()),
        INTERRUPTED(new InterruptedException()),
        UNSUPPORTED_ENCODING(new UnsupportedEncodingException()),
        IO(new IOException());
        
        private final Exception exception;

        private ExceptionType(Exception exception) {
            this.exception = exception;
        }

        public Exception getException() {
            return exception;
        }
    }
}
