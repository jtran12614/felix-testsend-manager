package com.rakuten.felix.testsend.manager;

import com.rakuten.felix.testsend.manager.validator.ValidationException;
import com.rakuten.felix.testsend.manager.validator.Validator;
import com.rakuten.felix.testsend.manager.web.dto.KickMailTestSendRequest;
import lombok.val;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatorTest {

    @Test
    void testValidateNull() {
        val objToValidate = new KickMailTestSendRequest(null, 1, null);
        assertThrows(ValidationException.class, () -> Validator.validate(objToValidate));
    }

    @Test
    void testValidateMin() {
        val objToValidate = new KickMailTestSendRequest(-1, 1, new JSONObject());
        assertThrows(ValidationException.class, () -> Validator.validate(objToValidate));
    }
}
