// @flow
import { StyleSheet } from 'react-native';
import { ColorPalette, DEFAULT_FONT_FAMILY } from '../../../common';

export default StyleSheet.create({
  button: {
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: ColorPalette.buttonColor,
    borderRadius: 15,
    paddingLeft: 20,
    paddingTop: 7,
    paddingRight: 20,
  },
  text: {
    borderRadius: 5,
    color: ColorPalette.white,
    fontSize: 16,
    fontFamily: DEFAULT_FONT_FAMILY,
  },
});
