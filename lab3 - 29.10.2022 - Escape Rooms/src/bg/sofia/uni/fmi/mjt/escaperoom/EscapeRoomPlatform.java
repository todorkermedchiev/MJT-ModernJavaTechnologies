package bg.sofia.uni.fmi.mjt.escaperoom;

import bg.sofia.uni.fmi.mjt.escaperoom.exception.PlatformCapacityExceededException;
import bg.sofia.uni.fmi.mjt.escaperoom.exception.RoomAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.escaperoom.exception.RoomNotFoundException;
import bg.sofia.uni.fmi.mjt.escaperoom.exception.TeamNotFoundException;
import bg.sofia.uni.fmi.mjt.escaperoom.room.EscapeRoom;
import bg.sofia.uni.fmi.mjt.escaperoom.room.Review;
import bg.sofia.uni.fmi.mjt.escaperoom.team.Team;

public class EscapeRoomPlatform implements EscapeRoomAdminAPI, EscapeRoomPortalAPI  {
    EscapeRoom[] rooms;
    private Team[] teams;
    private final int maxCapacity;
    private int roomsCount;

    public EscapeRoomPlatform(Team[] teams, int maxCapacity) {
        this.teams = teams;
        this.maxCapacity = maxCapacity;
        rooms = new EscapeRoom[maxCapacity];
        roomsCount = 0;
    }

    /**
     * Adds a new escape room to the platform.
     *
     * @param room the escape room to be added.
     * @throws IllegalArgumentException          if room is null.
     * @throws PlatformCapacityExceededException if the maximum number of escape rooms has already been reached.
     * @throws RoomAlreadyExistsException        if the specified room already exists in the platform.
     */
    @Override
    public void addEscapeRoom(EscapeRoom room) throws RoomAlreadyExistsException {
        if (roomsCount == maxCapacity) {
            throw new PlatformCapacityExceededException("The platform is full");
        }

        if (room == null) {
            throw new IllegalArgumentException("Variable room can not be null");
        }

        for (int i = 0; i < roomsCount; ++i) {
            if (rooms[i].getName().equals(room.getName())) {
                throw new RoomAlreadyExistsException("Room " + room.getName() + " aready exists");
            }
        }

        rooms[roomsCount++] = room;
    }

    /**
     * Removes the escape room with the specified name from the platform.
     *
     * @param roomName the name of the escape room to be removed.
     * @throws IllegalArgumentException if the room name is null, empty or blank.
     * @throws RoomNotFoundException    if the platform does not contain an escape room with the specified name.
     */
    @Override
    public void removeEscapeRoom(String roomName) throws RoomNotFoundException {
        validateString(roomName);

        for (int i = 0; i < roomsCount; ++i) {
            if (rooms[i].getName().equals(roomName)) {
                while (i < roomsCount - 1) {
                    rooms[i] = rooms[i + i];
                    ++i;
                }
                --roomsCount;
                return;
            }
        }
        throw new RoomNotFoundException("Room " + roomName + " doesn't exist");
    }

    /**
     * Returns all escape rooms contained in the platform.
     */
    @Override
    public EscapeRoom[] getAllEscapeRooms() {
        EscapeRoom[] newRooms = new EscapeRoom[roomsCount];
        System.arraycopy(rooms, 0, newRooms, 0, roomsCount);
        return newRooms;
    }

    /**
     * Registers a team achievement: escaping a room for the specified time.
     *
     * @param roomName   the name of the escape room.
     * @param teamName   the name of the team.
     * @param escapeTime the escape time in minutes.
     * @throws IllegalArgumentException if the room name or the team name is null, empty or blank,
     *                                  or if the escape time is negative, zero or bigger than the maximum time
     *                                  to escape for the specified room.
     * @throws RoomNotFoundException    if the platform does not contain an escape room with the specified name.
     */
    @Override
    public void registerAchievement(String roomName, String teamName, int escapeTime)
            throws RoomNotFoundException, TeamNotFoundException {
        validateString(roomName);
        validateString(teamName);

        EscapeRoom room = getEscapeRoomByName(roomName);
        Team team = getTeamByName(teamName);
        if (escapeTime <= 0 || escapeTime > room.getMaxTimeToEscape()) {
            throw new IllegalArgumentException("Invalid escape time");
        }

        int points = room.getDifficulty().getRank();
        if (escapeTime <= room.getMaxTimeToEscape() * 0.5) {
            points += 2;
        }
        else if (escapeTime <= room.getMaxTimeToEscape() * 0.75) {
            points += 1;
        }

        team.updateRating(points);
    }

    /**
     * Returns the escape room with the specified name.
     *
     * @param roomName the name of the escape room.
     * @return the escape room with the specified name.
     * @throws IllegalArgumentException if the room name is null, empty or blank
     * @throws RoomNotFoundException    if the platform does not contain an escape room with the specified name.
     */
    @Override
    public EscapeRoom getEscapeRoomByName(String roomName) throws RoomNotFoundException {
        validateString(roomName);

        for (int i = 0; i < roomsCount; ++i) {
            if (rooms[i].getName().equals(roomName)) {
                return rooms[i];
            }
        }
        throw new RoomNotFoundException("Room " + roomName + " doesn't exist");
    }

    /**
     * Adds a review for the escape room with the specified name.
     *
     * @param roomName the name of the escape room.
     * @throws IllegalArgumentException if the room name is null, empty or blank, or if the review is null
     * @throws RoomNotFoundException    if the platform does not contain an escape room with the specified name.
     */
    @Override
    public void reviewEscapeRoom(String roomName, Review review) throws RoomNotFoundException {
        validateString(roomName);
        if (review == null) {
            throw new IllegalArgumentException("Variable review can not be null");
        }
        getEscapeRoomByName(roomName).addReview(review);
    }

    /**
     * Returns all reviews for the escape room with the specified name, in the order they have been added.
     * If there are no reviews, returns an empty array.
     *
     * @param roomName the name of the escape room.
     * @return the reviews for the escape room with the specified name
     * @throws IllegalArgumentException if the room name is null, empty or blank, or if the review is null
     * @throws RoomNotFoundException    if the platform does not contain an escape room with the specified name.
     */
    @Override
    public Review[] getReviews(String roomName) throws RoomNotFoundException {
        validateString(roomName);

        return getEscapeRoomByName(roomName).getReviews();
    }

    /**
     * Returns the team with the highest rating. For each room successfully escaped (within the maximum
     * escape time), a team gets points equal to the room difficulty rank (1-4), plus bonus for fast escape:
     * +2 points for escape time less than or equal to 50% of the maximum escape time, or
     * +1 points for escape time less than or equal to 75% (and more than 50%) of the maximum escape time
     * The rating of a team is equal to the sum of all points collected.
     *
     * @return the top team by rating. If there are two or more teams with same highest rating, return any of them.
     * If there are no teams in the platform, returns null.
     */
    @Override
    public Team getTopTeamByRating() {
        int index = 0;
        for (int i = 0; i < teams.length; ++i) {
            if (teams[index].getRating() < teams[i].getRating()) {
                index = i;
            }
        }
        return teams[index];
    }

    private void validateString(String string) {
        if (string == null || string.isEmpty() || string.isBlank()) {
            throw new IllegalArgumentException("Invalid string argument");
        }
    }

    private Team getTeamByName(String teamName) throws TeamNotFoundException {
        validateString(teamName);
        for (Team team : teams) {
            if (team.getName().equals(teamName)) {
                return team;
            }
        }
        throw new TeamNotFoundException("Team " + teamName + " doesn't exist");
    }
}
