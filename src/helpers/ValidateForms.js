// @flow
import { strings } from '../../locales/i18n';

const EMAIL_REGEX = /\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*/;
const HALF_SIZE = new RegExp(/^[ｦ-ﾟ ､0-9a-zA-Z-_]*$/);

export const isEmpty = value =>
  typeof value === 'undefined' || value === null || value === '';

export const isMinValue = (min, value) => !isEmpty(value) && +value < min;

export const isMaxValue = (max, value) => !isEmpty(value) && +value > max;

export const isCharacterNumber = value => !/^-{0,1}\d+$/.test(value);

// export const required = (value, _, props, name) => {
//   const names = props.strings('validation.fields.' + name);
//   return isEmpty(value)
//     ? props.strings('validation.required', { names })
//     : undefined;
// };
export const required = (value, _, props, name) => {
  const titleField = strings(`validation.fields.${name}`);
  const textRequired = strings('validation.required');
  const msgRequired = `${titleField} ${textRequired}`;

  return isEmpty(value) ? `${msgRequired}` : undefined;
};

// export const maxLength = max => value =>
//   (value && value.length > max ? `Must be ${max} characters or less` : undefined);

export const minLength = (min, message) => (value, _, props) =>
  (!isEmpty(value) && value.length < min
    ? props.strings(message || "validation.minLength", { min })
    : undefined);

export const maxLength = max => (value, _, props) =>
  (!isEmpty(value) && value.length > max
    ? strings("validation.maxLength", { max })
    : undefined);

// export const minValue = (min, message) => (value, _, props) =>
//   (!isEmpty(value) && value < min
//     ? props.strings(message || "validation.minValue", { min })
//     : undefined);

// export const maxValue = (max, message) => (value, _, props) =>
//   (!isEmpty(value) && value > max
//     ? props.strings(message || "validation.maxValue", { max })
//     : undefined);

// export const number = (value, _, props) =>
//   (!isEmpty(value) && isNaN(Number(value))
//     ? props.strings("validation.number")
//     : undefined);

// export const regex = (pattern, message) => (value, _, props) =>
//   (!isEmpty(value) && typeof value === "string" && !pattern.test(value)
//     ? props.strings(message)
//     : undefined);

// export const email = regex(EMAIL_REGEX, 'validation.email');

// export const choices = (list, message) => (value, _, props) =>
//   (!isEmpty(value) && list.indexOf(value) === -1
//     ? props.strings(message)
//     : undefined);

// export const space = (value, _, props) =>
//   (value.indexOf(" ") !== -1 ? props.strings("validation.space") : undefined);

// export const halfSize = (value, _, props) =>
//   (value && !value.match(HALF_SIZE)
//     ? props.strings("validation.halfSize")
//     : undefined);

// export const maxLength32 = (value, _, props, name) => {
//   const names = props.strings('validation.fields.' + name);
//   return !isEmpty(value) && value.length > 32
//     ? props.strings('validation.maxLength32', { names })
//     : undefined;
// };

// export const requiredCharacterNumber = (value, _, props, name) => {
//   // regex only value character is Number
//   const names = props.strings('validation.fields.' + name);
//   return isCharacterNumber(value)
//     ? props.strings('validation.required', { names })
//     : undefined;
// };
