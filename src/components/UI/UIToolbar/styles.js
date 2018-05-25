// @flow
import { StyleSheet } from 'react-native';
import { ColorPalette } from '../../../common/colors';

export const styles = StyleSheet.create({
  toolbar: {
    height: 48,
    backgroundColor: ColorPalette.white,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  back: {
    margin: 16,
    width: 18,
    height: 12,
  },
  title: {
    color: ColorPalette.black,
    alignItems: 'center',
    fontSize: 16,
    flex: 1,
    marginRight: 48,
    textAlign: 'center',
  },
});
