// @flow
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { View, Image, Text, NativeModules } from 'react-native';
import { UIButton } from '../../components/UI';
// import { NavigationActions } from 'react-navigation';
// import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scrollview';
import { styles } from './styles';
import { img_ic_card } from '../../../assets';

export class ExposedToJava {
  alert(message) {
    alert(message);
  }
}

class CardView extends Component {
  render() {
    return (
      <View style={styles.container}>
        <Text style={{ margin: 10, fontSize: 16 }}>
          View your remaining balance, recent trips, and other information from
          contactless, public transit cards using your NFC Android phone{' '}
        </Text>
        <Image
          style={{
            height: 200,
            marginTop: 20,
            marginBottom: 10,
            padding: 20,
            alignItems: 'center',
            alignSelf: 'center',
          }}
          resizeMode="stretch"
          source={img_ic_card}
        />
        <UIButton
          buttonStyle={styles.button}
          onPress={() =>
            NativeModules.ActivityStarter.navigateToExample(
              this.props.accessToken,
            )
          }
          title="Scan"
        />
        <UIButton
          title="Support Card"
          buttonStyle={styles.button}
          onPress={() => {
            NativeModules.ActivityStarter.navigateToSupportCard();
          }}
        />
      </View>
    );
  }
}

const mapStatesToProps = state => ({
  accessToken: state.auth.accessToken,
});

const mapDispatchToProps = () => ({});
export const CardScreen = connect(mapStatesToProps, mapDispatchToProps)(
  CardView,
);
