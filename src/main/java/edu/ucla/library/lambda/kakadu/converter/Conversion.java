
package edu.ucla.library.lambda.kakadu.converter;

/**
 * A type of conversion. First pass will be simple, but future expansion could include LOSSY_80PERCENT,
 * LOSSY_90PERCENT, etc.
 */
public enum Conversion {
    LOSSY, LOSSLESS;
}
