package uk.ac.standrews.cs.population_linkage.helpers.jobq.expression;

import java.math.BigDecimal;
import org.junit.Test;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.DoubleExpression;
import uk.ac.standrews.cs.population_linkage.helpers.jobq.expressions.Expression;

import static org.assertj.core.api.Assertions.assertThat;

public class DoubleExpressionTest {

//    @Test
//    public void singleValueConstructor() {
//        DoubleExpression doubleExpression = new DoubleExpression(db(1.0));
//
//        assertThat(doubleExpression.getValues()).containsExactly(db(1.0));
//        assertThat(doubleExpression.getExpression()).isEqualTo("1.0");
//    }
//
//    @Test
//    public void singleValueExpressionConstructor() {
//        DoubleExpression doubleExpression = new DoubleExpression("1.0");
//
//        assertThat(doubleExpression.getValues()).containsExactly(db(1.0));
//        assertThat(doubleExpression.getExpression()).isEqualTo("1.0");
//    }
//
//    @Test
//    public void andExpressionConstructor() {
//        DoubleExpression doubleExpression = new DoubleExpression("1.0&2.7&7.1");
//
//        assertThat(doubleExpression.getValues()).containsExactlyInAnyOrder(db(1.0), db(2.7), db(7.1));
//        assertThat(doubleExpression.getExpression()).isEqualTo("1.0&2.7&7.1");
//    }
//
//    @Test
//    public void rangeExpressionConstructor() {
//        DoubleExpression doubleExpression = new DoubleExpression("1.0->2.0@0.5");
//
//        assertThat(doubleExpression.getValues()).containsExactlyInAnyOrder(db(1.0), db(1.5), db(2.0));
//        assertThat(doubleExpression.getExpression()).isEqualTo("1.0->2.0@0.5");
//    }
//
//    @Test
//    public void takeFromRangeExpression() {
//        DoubleExpression doubleExpression = new DoubleExpression("1.0->2.0@0.5");
//
//        assertThat(doubleExpression.getValues()).containsExactlyInAnyOrder(db(1.0), db(1.5), db(2.0));
//        assertThat(doubleExpression.getExpression()).isEqualTo("1.0->2.0@0.5");
//
//        Expression<BigDecimal> taken = doubleExpression.takeValue();
//        assertThat(taken.getValues()).containsExactly(db(1.0));
//        assertThat(taken.getExpression()).isEqualTo("1.0");
//
//        assertThat(doubleExpression.getValues()).containsExactlyInAnyOrder(db(1.5), db(2.0));
//        assertThat(doubleExpression.getExpression()).isEqualTo("1.5&2.0");
//    }
//
//    private BigDecimal db(double d) {
//        return new BigDecimal(d);
//    }

}
