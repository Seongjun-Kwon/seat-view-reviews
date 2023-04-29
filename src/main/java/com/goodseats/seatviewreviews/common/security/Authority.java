package com.goodseats.seatviewreviews.common.security;

import static com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.goodseats.seatviewreviews.domain.member.model.vo.MemberAuthority;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authority {

	MemberAuthority[] authorities() default {USER, ADMIN};
}
