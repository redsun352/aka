import type { SignumProfile } from './signum';

export type SoilCondition = 'clean' | 'mineralized' | 'highMineralization' | 'littered';

export interface RecommendationInput {
  soil: SoilCondition;
  targetGoal: 'general' | 'deep' | 'small' | 'separation';
}

export interface Recommendation {
  profile: SignumProfile;
  notes: string[];
}

export function recommend(input: RecommendationInput): Recommendation {
  const profile: SignumProfile = {
    program: 'P3',
    mode: 'RT-ST',
    sensitivity: 11,
    recoveryTime: 8,
    gain: 7,
    discriminationBorder: -24,
    mmSpeed: input.targetGoal === 'separation' ? 5 : 3,
    groundScale: 0,
    filterT: 15,
    filterA: 2,
  };

  const notes: string[] = ['Ground Balance should be performed before searching.'];

  if (input.soil === 'mineralized' || input.soil === 'highMineralization') {
    profile.sensitivity = 7;
    profile.gain = 5;
    notes.push('Reduce sensitivity/gain and re-check ground response.');
  }

  if (input.targetGoal === 'deep') {
    profile.recoveryTime = 6;
    profile.filterT = 12;
    profile.filterA = 1;
    notes.push('Use a slower sweep and confirm targets from multiple directions.');
  }

  if (input.targetGoal === 'small') {
    profile.sensitivity = Math.max(profile.sensitivity, 10);
    profile.recoveryTime = 5;
    notes.push('Prioritize stable response over maximum nominal sensitivity.');
  }

  if (input.soil === 'littered') {
    profile.mmSpeed = 5;
    profile.recoveryTime = 4;
    notes.push('Increase recovery speed for target separation in litter.');
  }

  return { profile, notes };
}
