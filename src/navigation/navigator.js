// @flow
import React from 'react';
import {
  Animated,
  Easing,
  Text,
  View,
  TouchableOpacity,
  Image,
} from 'react-native';
import {
  StackNavigator,
  DrawerNavigator,
  SwitchNavigator,
} from 'react-navigation';
// import { MainScreen } from '../features/main';
import { SplashScreen } from '../features/auth/splash';
import { LoginScreen, ForgotScreen } from '../features/auth';
import { ExpenseScreen } from '../features/expense';
import { EntertainmentScreen } from '../features/entertainment';
import { CreateExpenseScreen } from '../features/expense/createExpense/CreateExpenseScreen';
import { CardScreen } from '../features/card';
import { TrafficScreen } from '../features/traffic';
import { ic_menu } from '../../assets';
import { SideMenu } from './components/SideMenu';

const noTransitionConfig = () => ({
  transitionSpec: {
    duration: 0,
    timing: Animated.timing,
    easing: Easing.step0,
  },
});

export const expenseNavigator = StackNavigator(
  {
    expenseStack: { screen: ExpenseScreen },
  },
  {
    headerMode: 'float',
    navigationOptions: ({ navigation }) => ({
      headerStyle: { backgroundColor: '#6d77a2' },
      headerLeft: (
        <View style={{ flexDirection: 'row' }}>
          <TouchableOpacity onPress={() => navigation.navigate('DrawerOpen')}>
            <Image
              style={{
                marginLeft: 10,
                marginRight: 10,
                width: 24,
                height: 24,
                alignItems: 'center',
              }}
              source={ic_menu}
            />
          </TouchableOpacity>
          <Text style={{ color: '#fff', fontSize: 16, marginLeft: 20 }}>
            経費精算
          </Text>
        </View>
      ),
    }),
  },
);
export const entertainmentNavigator = StackNavigator(
  {
    entertainmentStack: { screen: EntertainmentScreen },
  },
  {
    headerMode: 'float',
    navigationOptions: ({ navigation }) => ({
      headerStyle: { backgroundColor: '#6d77a2' },
      headerLeft: (
        <View style={{ flexDirection: 'row' }}>
          <TouchableOpacity onPress={() => navigation.navigate('DrawerOpen')}>
            <Image
              style={{
                marginLeft: 10,
                marginRight: 10,
                width: 24,
                height: 24,
                alignItems: 'center',
              }}
              source={ic_menu}
            />
          </TouchableOpacity>
          <Text style={{ color: '#fff', fontSize: 16, marginLeft: 20 }}>
            交際費精算申請
          </Text>
        </View>
      ),
    }),
  },
);
export const trafficNavigator = StackNavigator(
  {
    applyPaymentStack: { screen: TrafficScreen },
  },
  {
    headerMode: 'float',
    navigationOptions: ({ navigation }) => ({
      headerStyle: { backgroundColor: '#6d77a2' },
      headerLeft: (
        <View style={{ flexDirection: 'row' }}>
          <TouchableOpacity onPress={() => navigation.navigate('DrawerOpen')}>
            <Image
              style={{
                marginLeft: 10,
                marginRight: 10,
                width: 24,
                height: 24,
                alignItems: 'center',
              }}
              source={ic_menu}
            />
          </TouchableOpacity>
          <Text style={{ color: '#fff', fontSize: 16, marginLeft: 20 }}>
            仮払い申請
          </Text>
        </View>
      ),
    }),
  },
);
export const cardNavigator = StackNavigator(
  {
    cardStack: { screen: CardScreen },
  },
  {
    headerMode: 'float',
    navigationOptions: ({ navigation }) => ({
      headerStyle: { backgroundColor: '#6d77a2' },
      headerLeft: (
        <View style={{ flexDirection: 'row' }}>
          <TouchableOpacity onPress={() => navigation.navigate('DrawerOpen')}>
            <Image
              style={{
                marginLeft: 10,
                marginRight: 10,
                width: 24,
                height: 24,
                alignItems: 'center',
              }}
              source={ic_menu}
            />
          </TouchableOpacity>
          <Text style={{ color: '#fff', fontSize: 16, marginLeft: 20 }}>
            IC Card
          </Text>
        </View>
      ),
    }),
  },
);

// drawer stack
const DrawerNavigation = DrawerNavigator(
  {
    expense: {
      screen: expenseNavigator,
    },
    applyExpense: {
      screen: entertainmentNavigator,
    },
    card: {
      screen: cardNavigator,
    },
    applyPayment: {
      screen: trafficNavigator,
    },
  },
  {
    contentComponent: SideMenu,
    drawerWidth: 320,
    transitionConfig: noTransitionConfig,
  },
);

// login stack
export const LoginStack = StackNavigator(
  {
    splash: { screen: SplashScreen },
    login: { screen: LoginScreen },
    forgot: { screen: ForgotScreen },
  },
  {
    // Default config for all screens
    headerMode: 'none',
    initialRouteName: 'splash',
  },
);

// login stack
export const MainStack = StackNavigator(
  {
    menu: { screen: DrawerNavigation },
    createExpense: { screen: CreateExpenseScreen },
  },
  {
    // Default config for all screens
    headerMode: 'none',
  },
);

// Manifest of possible screens
export const AppNavigator = SwitchNavigator(
  {
    loginStack: { screen: LoginStack },
    main: { screen: MainStack },
  },
  {
    // Default config for all screens
    headerMode: 'none',
    initialRouteName: 'loginStack',
  },
);
