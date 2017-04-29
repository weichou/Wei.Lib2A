/*
 * Copyright (C) 2014-present, Wei Chou (weichou2010@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hobby.wei.c.exception;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class SdCardNotValidException extends Exception {
	private static final long serialVersionUID = -5499911517665209427L;

	public SdCardNotValidException() {}
	
	public SdCardNotValidException(String message) {
		super(message);
	}
	
	public SdCardNotValidException(Throwable throwable) {
		super(throwable);
	}
	
	public SdCardNotValidException(String message, Throwable throwable) {
		super(message, throwable);
	}
} 
